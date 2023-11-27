package ru.nsu.ooad.proletorrent.torrent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentInfo;
import ru.nsu.ooad.proletorrent.exception.TorrentException;
import ru.nsu.ooad.proletorrent.exception.TrackerException;
import ru.nsu.ooad.proletorrent.exception.UnsupportedSchemeException;
import ru.nsu.ooad.proletorrent.service.TorrentListListener;

import javax.print.DocFlavor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class TorrentConnection implements Runnable {

    private static final String PSTR = (char) 0x13 + "BitTorrent protocol";
    private static final int BUFFER_SIZE = 8192;

    private final String peerId;
    private final TorrentInfo meta;
    private final TorrentListListener listener;
    private final String tracker;

    private TrackerManager manager;
    private byte[] infoHash;

    private Selector selector;

    public TorrentConnection(String peerId, TorrentInfo meta, TorrentListListener listener) throws UnsupportedSchemeException {
        this.peerId = peerId;
        this.meta = meta;
        this.listener = listener;

        tracker = meta.getName() != null && meta.getAnnounceList() != null &&
                    !meta.getAnnounce().startsWith("http") ? meta.getAnnounceList().stream()
                .filter(e -> e.startsWith("http"))
                .findFirst()
                .orElseThrow(UnsupportedSchemeException::new) : meta.getAnnounce();
        if (tracker == null) {
            throw new UnsupportedSchemeException("no supported scheme");
        }
    }

    public String getName() {
        return meta.getName();
    }

    public long getSize() {
        return meta.getTotalSize();
    }

    @Override
    public void run() {
        try {
            manager = new HttpTrackerManager(tracker);
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
                .state(PeerState.HANDSHAKING)
                .peer(peer)
                .buffer(ByteBuffer.allocate(BUFFER_SIZE))
                .build());
    }

    private List<Peer> getPeers() throws DecoderException, TrackerException, IOException {
        infoHash = Hex.decodeHex(meta.getInfoHash());
        log.info("GET " + tracker);
        AnnounceResponse response = manager.send(AnnounceRequest.builder()
                .port(6881).infoHash(infoHash).peerId(peerId)
                .uploaded(0).downloaded(0).left(meta.getTotalSize()).compact(true)
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
        switch (attachment.getState()) {
            case HANDSHAKING -> {
                ByteBuffer buffer = attachment.getBuffer();
                channel.write(createHandshake(buffer));
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    private void readPeer(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        PeerAttachment attachment = (PeerAttachment) key.attachment();
        switch (attachment.getState()) {
            case HANDSHAKING -> {
                if (!isValidHandshake(key)) {
                    closeKey(key);
                } else {
                    log.info("successful handshake with " + attachment.getPeer());
                    attachment.setState(PeerState.CHOKED);
                }
            }
            case CHOKED -> {
                ByteBuffer buffer = attachment.getBuffer();
                buffer.rewind();
                channel.read(buffer);
                buffer.flip();
                PeerMessage message = PeerMessage.buildFromByteBuffer(buffer);
                log.info(message.toString());
                closeKey(key);
            }
        }
    }

    private boolean isValidHandshake(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        PeerAttachment attachment = (PeerAttachment) key.attachment();
        ByteBuffer buffer = attachment.getBuffer();
        buffer.flip();
        channel.read(buffer);
        buffer.flip();
        for (byte b : PSTR.getBytes()) {
            if (buffer.get() != b) {
                return false;
            }
        }
        buffer.getLong();
        for (byte b : infoHash) {
            if (buffer.get() != b) {
                return false;
            }
        }
        return true;
    }

    private ByteBuffer createHandshake(ByteBuffer buffer) {
        buffer.rewind();
        buffer.put(PSTR.getBytes());
        buffer.put(new byte[]{0, 0, 0, 0, 0, 0, 0, 0});
        buffer.put(infoHash);
        buffer.put(peerId.getBytes());
        buffer.flip();
        return buffer;
    }

}
