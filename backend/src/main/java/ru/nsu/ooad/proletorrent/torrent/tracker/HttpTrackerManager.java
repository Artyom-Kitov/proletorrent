package ru.nsu.ooad.proletorrent.torrent.tracker;

import lombok.RequiredArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import ru.nsu.ooad.proletorrent.bencode.BencodeException;
import ru.nsu.ooad.proletorrent.bencode.parser.Reader;
import ru.nsu.ooad.proletorrent.bencode.parser.objects.*;
import ru.nsu.ooad.proletorrent.exception.TrackerException;
import ru.nsu.ooad.proletorrent.torrent.Peer;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class HttpTrackerManager implements TrackerManager {

    private static final Charset TORRENT_CHARSET = StandardCharsets.ISO_8859_1;
    private static final int TIMEOUT = 1000;

    private final String announceUrl;
    private final CloseableHttpClient client = HttpClientBuilder.create()
            .setDefaultRequestConfig(RequestConfig.custom()
                    .setConnectTimeout(TIMEOUT)
                    .build())
            .build();

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

        byte[] bytes = response.getEntity().getContent().readAllBytes();
        BencodeDictionary bencodeResponse;
        try {
            bencodeResponse = new Reader(new String(bytes)).readDictionary();
        } catch (BencodeException e) {
            throw new TrackerException("invalid response from tracker");
        }
        IBencodeObject error = bencodeResponse.find(new BencodeString("failure reason"));
        if (error != null) {
            throw new TrackerException(error.toString());
        }

        int interval = Math.toIntExact(((BencodeInteger) bencodeResponse.find(new BencodeString("interval"))).getValue());

        int start = indexOf(bytes, new byte[]{'p', 'e', 'e', 'r', 's'});
        byte[] peersBytes = Arrays.copyOfRange(bytes, start, bytes.length);
        String peersSubstr = new String(peersBytes);
        int peersLength = Integer.parseInt(peersSubstr.substring(5, peersSubstr.indexOf(':')));
        int beginIdx = peersSubstr.indexOf(':') + 1;

        byte[] peersRawBytes = Arrays.copyOfRange(peersBytes, beginIdx, beginIdx + peersLength);
        List<Peer> peers = new ArrayList<>();
        for (int i = 0; i < peersRawBytes.length; i += 6) {
            byte[] addr = Arrays.copyOfRange(peersRawBytes, i, i + 6);
            int port = ((0xFF & addr[4]) << 8) | (0xFF & addr[5]);
            peers.add(new Peer(Inet4Address.getByAddress(Arrays.copyOfRange(addr, 0, 4)), port));
        }
        return AnnounceResponse.builder()
                .interval(interval)
                .peers(peers)
                .build();
    }

    @Override
    public InetAddress getHost() {
        try {
            return InetAddress.getByName(new URL(announceUrl).getHost());
        } catch (MalformedURLException | UnknownHostException e) {
            return null;
        }
    }

    private int indexOf(byte[] source, byte[] dest) {
        int n = source.length;
        int m = dest.length;
        for (int i = 0; i <= n - m; i++) {
            int j;
            for (j = 0; j < m; j++) {
                if (source[i + j] != dest[j]) {
                    break;
                }
            }
            if (j == m) {
                return i;
            }
        }
        return -1;
    }

}
