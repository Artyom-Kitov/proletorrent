package ru.nsu.ooad.proletorrent.torrent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentInfo;
import ru.nsu.ooad.proletorrent.exception.TorrentException;
import ru.nsu.ooad.proletorrent.exception.UnsupportedSchemeException;
import ru.nsu.ooad.proletorrent.service.TorrentListListener;

import java.io.IOException;
import java.net.Socket;

@Slf4j
@RequiredArgsConstructor
public class TorrentConnection implements Runnable {

    private final String peerId;
    private final TorrentInfo meta;
    private final TorrentListListener listener;
    private final String tracker;

    private TrackerManager manager;

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
            byte[] infoHash = Hex.decodeHex(meta.getInfoHash());
            log.info("GET " + tracker);
            AnnounceResponse response = manager.send(AnnounceRequest.builder()
                    .port(6881).infoHash(infoHash).peerId(peerId)
                    .uploaded(0).downloaded(0).left(meta.getTotalSize()).compact(true)
                    .noPeerId(true).event(AnnounceRequest.RequestEvent.STARTED)
                    .numWant(50).build());
            log.info(response.toString());
            for (Peer peer : response.getPeers()) {
                try {
                    Socket socket = new Socket();
                    socket.connect(peer.getAddress(), 2000);
                    socket.close();
                    log.info(peer.getAddress() + " is alive");
                } catch (IOException e) {
                    log.error(peer.getAddress() + " is not alive");
                }
            }
        } catch (TorrentException | IOException | DecoderException e) {
            log.error(e.getMessage());
        } finally {
            listener.remove(peerId);
        }
    }

}
