package ru.nsu.ooad.proletorrent.service.impl;

import org.springframework.stereotype.Service;
import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentFile;
import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentInfo;
import ru.nsu.ooad.proletorrent.dto.TorrentFileTreeNode;
import ru.nsu.ooad.proletorrent.service.TorrentInfoService;

import java.util.Comparator;
import java.util.List;

@Service
public class TorrentInfoServiceImpl implements TorrentInfoService {

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

}
