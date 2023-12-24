package ru.nsu.ooad.proletorrent.torrent;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.data.util.Pair;
import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentInfo;
import ru.nsu.ooad.proletorrent.exception.TorrentException;
import ru.nsu.ooad.proletorrent.exception.TrackerException;
import ru.nsu.ooad.proletorrent.service.TorrentListListener;
import ru.nsu.ooad.proletorrent.torrent.tracker.AnnounceRequest;
import ru.nsu.ooad.proletorrent.torrent.tracker.AnnounceResponse;
import ru.nsu.ooad.proletorrent.torrent.tracker.TrackerManager;
import ru.nsu.ooad.proletorrent.torrent.utils.NotEnoughBytesException;
import ru.nsu.ooad.proletorrent.torrent.utils.PeerBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Slf4j
public class TorrentConnection implements Runnable {

    private static final int PIPELINE_LENGTH = 5;
    private static final int BUFFER_SIZE = 65536 * PIPELINE_LENGTH;
    private static final String PSTR = (char) 0x13 + "BitTorrent protocol";
    private static final int HANDSHAKE_SIZE = PSTR.length() + 48;

    @Getter
    private final String peerId;

    private final TorrentInfo meta;
    private final TorrentListListener listener;
    private final List<TrackerManager> managers;

    @Getter
    private final Instant createdAt;

    private TrackerManager aliveTracker;
    private byte[] infoHash;

    private Selector selector;
    private PieceQueue pieceQueue;

    private TorrentFileWriter writer;

    private int pollInterval;
    private Instant lastPolledAt;

    @Getter
    private long bytesDownloaded;

    @Builder
    public TorrentConnection(String peerId, TorrentInfo meta, List<TrackerManager> managers,
                             Instant createdAt, TorrentListListener listener) {
        this.peerId = peerId;
        this.meta = meta;
        this.managers = managers;
        this.createdAt = createdAt;
        this.listener = listener;
    }

    public String getName() {
        return meta.getName();
    }

    public long getSize() {
        return meta.getTotalSize();
    }

    @Override
    public void run() {
        pieceQueue = new PieceQueue(meta.getPieces(), Math.toIntExact(meta.getPieceLength()));
        log.info(meta.getPieces().size() + " pieces of length " + meta.getPieceLength());
        try {
            writer = new TorrentFileWriter(meta.getName(), Math.toIntExact(meta.getPieceLength()));
            List<Peer> peers = getPeers();
            selector = Selector.open();
            for (Peer peer : peers) {
                registerPeerKey(peer);
            }
            while (selector.select() > 0) {
                Set<SelectionKey> keys = selector.selectedKeys();
                handleKeys(keys);
            }
        } catch (TorrentException | DecoderException e) {
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
            if (selector != null) {
                try {
                    writer.close();
                    selector.close();
                } catch (IOException ex) {
                    log.error(ex.getMessage());
                }
            }
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            listener.remove(peerId);
        }
    }

    private void registerPeerKey(Peer peer) throws IOException {
        SocketChannel peerChannel = SocketChannel.open();
        peerChannel.configureBlocking(false);
        peerChannel.connect(peer.address());
        SelectionKey key = peerChannel.register(selector, SelectionKey.OP_CONNECT);
        key.attach(PeerAttachment.builder()
                .peer(peer)
                .buffer(new PeerBuffer(BUFFER_SIZE))
                .build());
    }

    private List<Peer> getPeers() throws DecoderException, TrackerException {
        infoHash = Hex.decodeHex(meta.getInfoHash());
        for (TrackerManager manager : managers) {
            try {
                AnnounceResponse response = announce(manager);
                log.info(response.toString());
                pollInterval = response.getInterval();
                log.info("interval: " + pollInterval + "s");
                lastPolledAt = Instant.now();
                aliveTracker = manager;
                return response.getPeers();
            } catch (IOException | TrackerException e) {
                log.warn("tracker " + manager.getHost() + " is not alive");
            }
        }
        throw new TrackerException("no alive trackers");
    }

    private List<Peer> pollAliveTracker() {
        try {
            AnnounceResponse response = announce(aliveTracker);
            log.info(response.toString());
            pollInterval = response.getInterval();
            lastPolledAt = Instant.now();
            return response.getPeers();
        } catch (IOException | TrackerException e) {
            log.warn("tracker " + aliveTracker.getHost() + " died");
            return List.of();
        }
    }

    private AnnounceResponse announce(TrackerManager manager) throws TrackerException, IOException {
        return manager.send(AnnounceRequest.builder()
                .port(6881).infoHash(infoHash).peerId(peerId)
                .uploaded(0).downloaded(0).left(meta.getTotalSize() == null ? 0 : meta.getTotalSize()).compact(true)
                .noPeerId(true).event(AnnounceRequest.RequestEvent.STARTED)
                .numWant(50).build());
    }

    private void handleKeys(Set<SelectionKey> keys) {
        if (lastPolledAt.plus(Duration.ofSeconds(pollInterval)).isBefore(Instant.now())) {
            List<Peer> newPeers = pollAliveTracker();
            for (Peer peer : newPeers) {
                try {
                    registerPeerKey(peer);
                } catch (IOException e) {
                    log.error("error registering new peer: " + e.getMessage());
                }
            }
        }
        Iterator<SelectionKey> iterator = keys.iterator();
        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            iterator.remove();
            try {
                if (!key.isValid()) {
                    closeKey(key);
                    continue;
                }
                if (key.isConnectable()) {
                    connectPeer(key);
                } else if (key.isReadable()) {
                    readPeer(key);
                } else if (key.isWritable()) {
                    writePeer(key);
                } else {
                    closeKey(key);
                }
            } catch (IOException e) {
                log.error(e.getMessage());
                try {
                    closeKey(key);
                } catch (IOException ex) {
                    log.error(ex.getMessage());
                }
            }
        }
    }

    private void closeKey(SelectionKey key) throws IOException {
        key.channel().close();
        key.cancel();
    }

    private void connectPeer(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        PeerAttachment attachment = (PeerAttachment) key.attachment();
        if (!channel.finishConnect()) {
            closeKey(key);
        }
        log.info("connected to " + attachment.getPeer());
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void writePeer(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        PeerAttachment attachment = (PeerAttachment) key.attachment();
        if (!attachment.isApproved()) {
            channel.write(createHandshake());
            key.interestOps(SelectionKey.OP_READ);
            return;
        }
        if (!attachment.isInterested()) {
            sendMessage(key, PeerMessage.builder()
                    .type(PeerMessage.Type.INTERESTED)
                    .build());
            attachment.setInterested(true);
            return;
        }
        for (int i = 0; i < PIPELINE_LENGTH; i++) {
            askPiece(key);
        }
        key.interestOps(SelectionKey.OP_READ);
    }

    private void readPeer(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        PeerAttachment attachment = (PeerAttachment) key.attachment();
        attachment.getBuffer().readFromChannel(channel);

        if (!attachment.isApproved()) {
            try {
                if (isValidHandshake(key)) {
                    log.info("successful handshake with " + attachment.getPeer());
                    attachment.setApproved(true);
                    key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                } else {
                    closeKey(key);
                    return;
                }
            } catch (NotEnoughBytesException e) {
                log.error(attachment.getPeer() + ": invalid handshake");
                closeKey(key);
            }
        }

        List<PeerMessage> messages = attachment.getBuffer().getMessages();
        if (messages.isEmpty()) {
            return;
        }
        handleMessages(key, messages);
    }

    private void handleMessages(SelectionKey key, List<PeerMessage> messages) throws IOException {
        PeerAttachment attachment = (PeerAttachment) key.attachment();
        for (PeerMessage message : messages) {
            switch (message.type()) {
                case CHOKE -> {
                    attachment.setUnchoked(false);
                    key.interestOps(SelectionKey.OP_READ);
                }
                case UNCHOKE -> {
                    attachment.setUnchoked(true);
                    key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                }
                case KEEP_ALIVE -> {}
                case BITFIELD -> pieceQueue.addPiecesByMask(message.payload(), attachment.getPeer());
                case HAVE -> {
                    int index = ByteBuffer.wrap(message.payload()).getInt();
                    pieceQueue.addPiece(index, Set.of(attachment.getPeer()));
                }
                case PIECE -> {
                    ByteBuffer buffer = ByteBuffer.wrap(message.payload());
                    int piece = buffer.getInt();
                    Piece pending = pieceQueue.getPendingPiece();
                    if (pending.getIndex() != piece) {
                        continue;
                    }
                    int offset = buffer.getInt();
                    pending.writePart(message.payload(), offset);
                    key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    if (pending.isComplete() && pending.isValidHash()) {
                        if (!pending.isValidHash()) {
                            log.error(meta.getName() + ": piece #" + pending.getIndex() + ": hashes don't match");
                            pieceQueue.remove();
                            pieceQueue.addPiece(pending.getIndex(), pending.getPeers());
                        } else {
                            log.info("got piece #" + pending.getIndex());
                            writer.write(pending.getData(), pending.getIndex());
                            bytesDownloaded += pending.getData().length;
                            pieceQueue.remove();
                            if (bytesDownloaded == meta.getTotalSize() && pieceQueue.isDownloaded()) {
                                log.info(meta.getName() + " successfully downloaded!");
                                stopUpload();
                            }
                        }
                    }
                }
                default -> log.info(String.valueOf(message));
            }
        }
    }

    private void stopUpload() throws IOException {
        writer.close();
        selector.close();
        listener.onUpload(peerId, meta.getName(), writer.getTotalDownloaded(), writer.getFilePath());
    }

    private void sendMessage(SelectionKey key, PeerMessage message) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.write(message.toByteBuffer());
    }

    private void askPiece(SelectionKey key) throws IOException {
        Piece piece = pieceQueue.getPendingPiece();
        if (piece == null) {
            return;
        }
        PeerAttachment attachment = (PeerAttachment) key.attachment();
        if (!piece.getPeers().contains(attachment.getPeer())) {
            return;
        }
        Pair<Integer, Integer> part = piece.getEmptyPart();
        if (part == null) {
            return;
        }
        byte[] payload = new byte[12];
        ByteBuffer b = ByteBuffer.wrap(payload);
        b.putInt(piece.getIndex()).putInt(part.getFirst()).putInt(part.getSecond());
        sendMessage(key, PeerMessage.builder()
                .type(PeerMessage.Type.REQUEST)
                .payload(payload)
                .build());
    }

    private boolean isValidHandshake(SelectionKey key) throws NotEnoughBytesException {
        PeerAttachment attachment = (PeerAttachment) key.attachment();
        PeerBuffer buffer = attachment.getBuffer();

        ByteBuffer handshake = buffer.getHandshake(HANDSHAKE_SIZE);
        for (byte b : PSTR.getBytes()) {
            if (handshake.get() != b) {
                return false;
            }
        }
        handshake.getLong();
        for (byte b : infoHash) {
            if (handshake.get() != b) {
                return false;
            }
        }
        return true;
    }

    private ByteBuffer createHandshake() {
        ByteBuffer handshake = ByteBuffer.allocate(HANDSHAKE_SIZE);
        handshake.put(PSTR.getBytes());
        handshake.putLong(0);
        handshake.put(infoHash);
        handshake.put(peerId.getBytes());
        handshake.flip();
        return handshake;
    }

}
