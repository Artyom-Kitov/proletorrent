package ru.nsu.ooad.proletorrent.torrent;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Slf4j
@Builder
public class TorrentConnection implements Runnable {

    private static final String PSTR = (char) 0x13 + "BitTorrent protocol";
    private static final int BUFFER_SIZE = 8192;
    private static final int HANDSHAKE_SIZE = PSTR.length() + 48;

    private final String peerId;
    private final TorrentInfo meta;
    private final TorrentListListener listener;
    private final TrackerManager manager;
    private byte[] infoHash;

    private Selector selector;

    public String getName() {
        return meta.getName();
    }

    public long getSize() {
        return meta.getTotalSize();
    }

    @Override
    public void run() {
        try {
            List<Peer> peers = getPeers();
            selector = Selector.open();
//            registerPeerKey(new Peer("90.161.164.173", 24592));
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
                    selector.close();
                } catch (IOException ignore) {
                }
            }
        } finally {
            listener.remove(peerId);
        }
    }

    private void registerPeerKey(Peer peer) throws IOException {
        SocketChannel peerChannel = SocketChannel.open();
        peerChannel.configureBlocking(false);
        peerChannel.connect(peer.getAddress());
        SelectionKey key = peerChannel.register(selector, SelectionKey.OP_CONNECT);
        key.attach(PeerAttachment.builder()
                .peer(peer)
                .buffer(new PeerBuffer(BUFFER_SIZE))
                .build());
    }

    private List<Peer> getPeers() throws DecoderException, TrackerException, IOException {
        infoHash = Hex.decodeHex(meta.getInfoHash());
        AnnounceResponse response = manager.send(AnnounceRequest.builder()
                .port(6881).infoHash(infoHash).peerId(peerId)
                .uploaded(0).downloaded(0).left(meta.getTotalSize() == null ? 0 : meta.getTotalSize()).compact(true)
                .noPeerId(true).event(AnnounceRequest.RequestEvent.STARTED)
                .numWant(50).build());
        log.info(response.toString());
        return response.getPeers();
    }

    private void handleKeys(Set<SelectionKey> keys) {
        Iterator<SelectionKey> iterator = keys.iterator();
        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            iterator.remove();
            try {
                if (!key.isValid()) {
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
//            sendMessage(key, PeerMessage.builder()
//                    .type(PeerMessage.Type.INTERESTED)
//                    .build());
//            key.interestOps(SelectionKey.OP_READ);
        }
//        } else if (!attachment.isInterested()) {
//            PeerMessage interest = PeerMessage.builder()
//                    .type(PeerMessage.Type.INTERESTED)
//                    .build();
//            log.info(interest.toString());
//            channel.write(interest.toByteBuffer());
//            attachment.setInterested(true);
//            key.interestOps(SelectionKey.OP_READ);
//        }
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
            } catch (NotEnoughBytesException ignore) {
            }
        }

        List<PeerMessage> messages = attachment.getBuffer().getMessages();
        log.info(messages.toString());
//        PeerMessage message = readMessage(key);
//        log.info(message.toString());
//        if (message.type() == PeerMessage.Type.UNCHOKE && !attachment.isUnchoked()) {
//            attachment.setUnchoked(true);
//            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
//        }
    }

//    private PeerMessage readMessage(SelectionKey key) throws IOException {
//        SocketChannel channel = (SocketChannel) key.channel();
//        PeerAttachment attachment = (PeerAttachment) key.attachment();
//
//        attachment.getBuffer().clear();
//        channel.read(attachment.getBuffer());
//        attachment.getBuffer().flip();
//        try {
//            return PeerMessage.buildFromByteBuffer(attachment.getBuffer());
//        } catch (IllegalArgumentException e) {
//            System.out.println(channel.getRemoteAddress());
//            System.out.println(Arrays.toString(attachment.getBuffer().array()));
//            throw e;
//        }
//    }

    private void sendMessage(SelectionKey key, PeerMessage message) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.write(message.toByteBuffer());
    }

    private boolean isValidHandshake(SelectionKey key) throws NotEnoughBytesException {
        PeerAttachment attachment = (PeerAttachment) key.attachment();
        PeerBuffer buffer = attachment.getBuffer();

        ByteBuffer handshake = buffer.getHandshake(HANDSHAKE_SIZE);
        System.out.println("handshake: " + Arrays.toString(handshake.array()));
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
