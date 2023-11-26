package ru.nsu.ooad.proletorrent.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentFile;
import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentInfo;
import ru.nsu.ooad.proletorrent.dto.TorrentFileTreeNode;
import ru.nsu.ooad.proletorrent.exception.TrackerException;
import ru.nsu.ooad.proletorrent.service.TorrentService;
import ru.nsu.ooad.proletorrent.torrent.AnnounceRequest;
import ru.nsu.ooad.proletorrent.torrent.AnnounceResponse;
import ru.nsu.ooad.proletorrent.torrent.HttpTrackerManager;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class TorrentServiceImpl implements TorrentService {

    @Value("${torrent.peer-id.prefix}")
    private String peerIdPrefix;

    @Value("${torrent.peer-id.suffix-length}")
    private int suffixLength;

    @Override
    public TorrentFileTreeNode getTorrentFileStructure(TorrentInfo torrent) {
        if (torrent.isSingleFileTorrent()) {
            return TorrentFileTreeNode.builder()
                    .name(torrent.getName())
                    .size(torrent.getTotalSize())
                    .build();
        }
        long size = torrent.getFileList().stream()
                .map(TorrentFile::getFileSize)
                .reduce(Long::sum)
                .orElseThrow();
        TorrentFileTreeNode node = TorrentFileTreeNode.builder()
                .name(torrent.getName())
                .size(size)
                .build();
        for (TorrentFile file : torrent.getFileList()) {
            List<String> path = file.getFileInsides();
            node.addFile(path, file.getFileSize());
        }
        return node;
    }

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
