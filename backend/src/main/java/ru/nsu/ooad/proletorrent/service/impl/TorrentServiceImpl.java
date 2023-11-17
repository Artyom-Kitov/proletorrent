package ru.nsu.ooad.proletorrent.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import ru.nsu.ooad.proletorrent.bencode.parser.Reader;
import ru.nsu.ooad.proletorrent.bencode.parser.objects.BencodeDictionary;
import ru.nsu.ooad.proletorrent.bencode.parser.objects.BencodeString;
import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentInfo;
import ru.nsu.ooad.proletorrent.exception.TrackerException;
import ru.nsu.ooad.proletorrent.service.TorrentService;
import ru.nsu.ooad.proletorrent.torrent.AnnounceRequest;
import ru.nsu.ooad.proletorrent.torrent.AnnounceResponse;
import ru.nsu.ooad.proletorrent.torrent.HttpTrackerManager;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class TorrentServiceImpl implements TorrentService {

    @Value("${torrent.peer-id.prefix}")
    private String peerIdPrefix;

    @Value("${torrent.peer-id.suffix-length}")
    private int suffixLength;

    private static final Charset TORRENT_CHARSET = StandardCharsets.ISO_8859_1;

    private final CloseableHttpClient client = HttpClients.createDefault();

    @Override
    public void startUpload(TorrentInfo metaInfo) throws DecoderException {
        byte[] infoHash = Hex.decodeHex(metaInfo.getInfoHash());
        String peerId = generatePeerId();

        String tracker = (metaInfo.getAnnounce().startsWith("udp") ? metaInfo.getAnnounceList().stream()
                .filter(e -> e.startsWith("http"))
                .findFirst()
                .orElseThrow() : metaInfo.getAnnounce());
        try {
            AnnounceResponse response = new HttpTrackerManager(tracker).send(AnnounceRequest.builder()
                            .port(6881)
                            .infoHash(infoHash)
                            .peerId(peerId)
                            .uploaded(0)
                            .downloaded(0)
                            .left(0)
                            .compact(true)
                            .noPeerId(false)
                            .event(AnnounceRequest.RequestEvent.NONE)
                            .numWant(50).build());
            log.info(response.toString());
        } catch (IOException | TrackerException e) {
            throw new RuntimeException(e);
        }
//        String uri = (metaInfo.getAnnounce().startsWith("udp") ? metaInfo.getAnnounceList().stream()
//                .filter(e -> e.startsWith("http"))
//                .findFirst()
//                .orElseThrow() : metaInfo.getAnnounce())
//                + "?port=6881"
//                + "&info_hash=" + URLEncoder.encode(new String(infoHash, TORRENT_CHARSET), TORRENT_CHARSET)
//                + "&peer_id=" + peerId
//                + "&uploaded=0"
//                + "&downloaded=0"
//                + "&left=" + metaInfo.getTotalSize()
//                + "&compact=1";

//        HttpGet request = new HttpGet(uri);
//        HttpResponse response = null;
//        try {
//            response = client.execute(request);
//            BencodeDictionary bencodeResponse = new Reader(response.getEntity().getContent()).readDictionary();
//            System.out.println(bencodeResponse.bencodedString());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    private String generatePeerId() {
        String charSet = "0123456789abcdef";
        StringBuilder peerId = new StringBuilder(peerIdPrefix);
        for (int i = 0; i < suffixLength; i++) {
            peerId.append(charSet.charAt(new Random().nextInt(charSet.length())));
        }
        return peerId.toString();
    }

    public static String urlencode(byte[] unencodedBytes) {

        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < unencodedBytes.length; i++) {

            if (((unencodedBytes[i] >= 'a') && (unencodedBytes[i] <= 'z'))
                    || ((unencodedBytes[i] >= 'A') && (unencodedBytes[i] <= 'Z'))
                    || ((unencodedBytes[i] >= '0') && (unencodedBytes[i] <= '9')) || (unencodedBytes[i] == '.')
                    || (unencodedBytes[i] == '-') || (unencodedBytes[i] == '*') || (unencodedBytes[i] == '_')) {
                buffer.append((char) unencodedBytes[i]);
            } else if (unencodedBytes[i] == ' ') {
                buffer.append('+');
            } else {
                buffer.append(String.format("%%%02x", unencodedBytes[i]));
            }

        }

        return buffer.toString();

    }

}
