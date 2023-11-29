package ru.nsu.ooad.proletorrent.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentFile;
import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentInfo;
import ru.nsu.ooad.proletorrent.dto.TorrentFileTreeNode;
import ru.nsu.ooad.proletorrent.dto.TorrentStatusResponse;
import ru.nsu.ooad.proletorrent.exception.TorrentException;
import ru.nsu.ooad.proletorrent.repository.TorrentRepository;
import ru.nsu.ooad.proletorrent.repository.document.DownloadedTorrent;
import ru.nsu.ooad.proletorrent.service.TorrentListListener;
import ru.nsu.ooad.proletorrent.service.TorrentService;
import ru.nsu.ooad.proletorrent.torrent.TorrentConnection;
import ru.nsu.ooad.proletorrent.torrent.tracker.TrackerManager;
import ru.nsu.ooad.proletorrent.torrent.tracker.TrackerManagerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TorrentServiceImpl implements TorrentService, TorrentListListener {

    @Value("${torrent.peer-id.prefix}")
    private String peerIdPrefix;

    @Value("${torrent.peer-id.suffix-length}")
    private int suffixLength;

    private final TorrentRepository torrentRepository;

    private final ConcurrentHashMap<String, TorrentConnection> connections = new ConcurrentHashMap<>();

    @Override
    public List<TorrentStatusResponse> getStatuses() {
        List<TorrentStatusResponse> result = new ArrayList<>();
        Enumeration<TorrentConnection> pending = connections.elements();
        int counter = 0;
        for (Iterator<TorrentConnection> it = pending.asIterator(); it.hasNext(); counter++) {
            TorrentConnection connection = it.next();
            result.add(TorrentStatusResponse.builder()
                    .id(counter)
                    .name(connection.getName())
                    .size(connection.getSize())
                    .progress(69)
                    .status(TorrentStatusResponse.Status.DOWNLOADING.getValue())
                    .build());
        }
        List<DownloadedTorrent> downloaded = torrentRepository.findAll();
        for (DownloadedTorrent t : downloaded) {
            result.add(TorrentStatusResponse.builder()
                    .id(counter)
                    .name(t.getName())
                    .size(t.getSize())
                    .progress(100)
                    .status(TorrentStatusResponse.Status.SHARING.getValue())
                    .build());
            counter++;
        }
        return result;
    }

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
    public void startUpload(TorrentInfo metaInfo) throws TorrentException {
        String peerId = generatePeerId();
        TrackerManager manager = TrackerManagerFactory.getByTracker(metaInfo.getAnnounce(), metaInfo.getAnnounceList());
        TorrentConnection connection = TorrentConnection.builder()
                .peerId(peerId)
                .meta(metaInfo)
                .manager(manager)
                .listener(this)
                .build();
        connections.put(peerId, connection);
        new Thread(connection).start();
    }

    private String generatePeerId() {
        String charSet = "0123456789abcdef";
        StringBuilder peerId = new StringBuilder(peerIdPrefix);
        for (int i = 0; i < suffixLength; i++) {
            peerId.append(charSet.charAt(new Random().nextInt(charSet.length())));
        }
        return peerId.toString();
    }

    @Override
    public void remove(String key) {
        connections.remove(key);
    }

}
