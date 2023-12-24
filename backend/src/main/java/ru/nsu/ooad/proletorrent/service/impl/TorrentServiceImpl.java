package ru.nsu.ooad.proletorrent.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentFile;
import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentInfo;
import ru.nsu.ooad.proletorrent.dto.TorrentFileTreeNode;
import ru.nsu.ooad.proletorrent.dto.TorrentStatusResponse;
import ru.nsu.ooad.proletorrent.exception.InvalidTorrentException;
import ru.nsu.ooad.proletorrent.exception.NoSuchTorrentException;
import ru.nsu.ooad.proletorrent.exception.TorrentException;
import ru.nsu.ooad.proletorrent.exception.TorrentExistsException;
import ru.nsu.ooad.proletorrent.repository.TorrentRepository;
import ru.nsu.ooad.proletorrent.repository.document.DownloadedTorrent;
import ru.nsu.ooad.proletorrent.service.TorrentListListener;
import ru.nsu.ooad.proletorrent.service.TorrentService;
import ru.nsu.ooad.proletorrent.torrent.TorrentConnection;
import ru.nsu.ooad.proletorrent.torrent.tracker.TrackerManager;
import ru.nsu.ooad.proletorrent.torrent.tracker.TrackerManagerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TorrentServiceImpl implements TorrentService, TorrentListListener {

    private static final Random RANDOM = new Random();

    @Value("${torrent.peer-id.prefix}")
    private String peerIdPrefix;

    @Value("${torrent.peer-id.suffix-length}")
    private int suffixLength;

    private final TorrentRepository torrentRepository;

    private final ConcurrentHashMap<String, TorrentConnection> connections = new ConcurrentHashMap<>();

    @Override
    public long getTorrentSize(String name) {
        Optional<DownloadedTorrent> torrent = torrentRepository.findByName(name);
        return torrent.map(DownloadedTorrent::getSize).orElse(-1L);
    }

    @Override
    public List<TorrentStatusResponse> getStatuses() {
        List<TorrentStatusResponse> result = new ArrayList<>();
        Enumeration<TorrentConnection> pending = connections.elements();
        for (Iterator<TorrentConnection> it = pending.asIterator(); it.hasNext(); ) {
            TorrentConnection connection = it.next();
            result.add(TorrentStatusResponse.builder()
                    .id(connection.getPeerId())
                    .name(connection.getName())
                    .size(connection.getSize())
                    .progress(100.0 * connection.getBytesDownloaded() / connection.getSize())
                    .createdAt(connection.getCreatedAt())
                    .status(TorrentStatusResponse.Status.DOWNLOADING.getValue())
                    .build());
        }
        List<DownloadedTorrent> downloaded = torrentRepository.findAll();
        for (DownloadedTorrent t : downloaded) {
            result.add(TorrentStatusResponse.builder()
                    .id(t.getId())
                    .name(t.getName())
                    .size(t.getSize())
                    .progress(100)
                    .createdAt(t.getCreatedAt())
                    .status(TorrentStatusResponse.Status.SHARING.getValue())
                    .build());
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
                .reduce(0L, Long::sum);
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
        if (torrentRepository.existsByName(metaInfo.getName())) {
            throw new TorrentExistsException("torrent is already downloaded");
        }
        for (Iterator<TorrentConnection> it = connections.elements().asIterator(); it.hasNext(); ) {
            TorrentConnection connection = it.next();
            if (connection.getName().equals(metaInfo.getName())) {
                throw new TorrentExistsException("torrent is already being downloaded");
            }
        }
        if (!metaInfo.isSingleFileTorrent()) {
            metaInfo.setTotalSize(metaInfo.getFileList().stream()
                    .map(TorrentFile::getFileSize)
                    .reduce(0L, Long::sum));
        }
        String peerId = generatePeerId();
        List<TrackerManager> managers = TrackerManagerFactory.of(metaInfo.getAnnounce(), metaInfo.getAnnounceList());
        if (managers.isEmpty()) {
            throw new InvalidTorrentException("no tracker provided");
        }
        TorrentConnection connection = TorrentConnection.builder()
                .peerId(peerId)
                .meta(metaInfo)
                .managers(managers)
                .createdAt(Instant.now())
                .listener(this)
                .build();
        connections.put(peerId, connection);
        new Thread(connection).start();
    }

    @Override
    public StreamingResponseBody download(String name) throws NoSuchTorrentException, FileNotFoundException {
        DownloadedTorrent torrent = torrentRepository.findByName(name)
                .orElseThrow(() -> new NoSuchTorrentException("no such downloaded torrent"));
        Path path = Path.of(torrent.getFullPath());
        if (!Files.exists(path)) {
            torrentRepository.delete(torrent);
            throw new NoSuchTorrentException("no such downloaded torrent");
        }
        FileInputStream inputStream = new FileInputStream(path.toFile());
        return outputStream -> {
            byte[] buffer = new byte[8192];
            int bytesRead = 0;
            while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
        };
    }

    private String generatePeerId() {
        String charSet = "0123456789abcdef";
        StringBuilder peerId = new StringBuilder(peerIdPrefix);
        for (int i = 0; i < suffixLength; i++) {
            peerId.append(charSet.charAt(RANDOM.nextInt(charSet.length())));
        }
        return peerId.toString();
    }

    @Override
    public void remove(String key) {
        connections.remove(key);
    }

    @Override
    public void onUpload(String key, String name, long size, Path fullPath, Instant createdAt) {
        torrentRepository.save(DownloadedTorrent.builder()
                        .id(key)
                        .name(name)
                        .size(size)
                        .createdAt(createdAt)
                        .fullPath(fullPath.toString())
                .build());
    }

}
