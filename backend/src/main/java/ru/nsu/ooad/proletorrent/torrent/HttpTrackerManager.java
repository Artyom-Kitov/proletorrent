package ru.nsu.ooad.proletorrent.torrent;

import lombok.RequiredArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import ru.nsu.ooad.proletorrent.bencode.parser.Reader;
import ru.nsu.ooad.proletorrent.bencode.parser.objects.*;
import ru.nsu.ooad.proletorrent.exception.TrackerException;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class HttpTrackerManager implements TrackerManager {

    private static final Charset TORRENT_CHARSET = StandardCharsets.ISO_8859_1;

    private final String announceUrl;
    private final CloseableHttpClient client = HttpClients.createDefault();

    @Override
    public AnnounceResponse send(AnnounceRequest request) throws IOException, TrackerException {
        String url = announceUrl
                + "?info_hash=" + URLEncoder.encode(new String(request.getInfoHash(), TORRENT_CHARSET), TORRENT_CHARSET)
                + "&peer_id=" + request.getPeerId()
                + "&port=" + request.getPort()
                + "&uploaded=" + request.getUploaded()
                + "&downloaded=" + request.getDownloaded()
                + "&left=" + request.getLeft()
                + "&compact=" + (request.isCompact() ? "1" : "0")
                + "&no_peer_id=" + (request.isNoPeerId() ? "1" : "0")
                + "&event=" + request.getEvent().getName()
                + "&numwant=" + request.getNumWant()
                + (request.getTrackerId() == null ? "" : "&trackerid=" + request.getTrackerId());
        HttpGet trackerRequest = new HttpGet(url);
        HttpResponse response = client.execute(trackerRequest);
        BencodeDictionary bencodeResponse = new Reader(response.getEntity().getContent()).readDictionary();
        IBencodeObject error = bencodeResponse.find(new BencodeString("failure reason"));
        if (error != null) {
            throw new TrackerException(error.toString());
        }

        int interval = Math.toIntExact(((BencodeInteger) bencodeResponse.find(new BencodeString("interval"))).getValue());
//        String trackerId = bencodeResponse.find(new BencodeString("tracker id"));
        byte[] peersBytes = bencodeResponse.find(new BencodeString("peers")).bencode();
        List<Peer> peers = new ArrayList<>();
        for (int i = 0; i < 6 * request.getNumWant(); i += 6) {
            int port = ((0xFF & peersBytes[i + 4]) << 8) | (0xFF & peersBytes[i + 5]);
            peers.add(new Peer(Inet4Address.getByAddress(Arrays.copyOfRange(peersBytes, i, i + 4)), port));
        }
        return AnnounceResponse.builder()
                .interval(interval)
                .peers(peers)
//                .trackerId(trackerId)
                .build();
    }

}
