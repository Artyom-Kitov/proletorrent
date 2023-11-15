package ru.nsu.ooad.proletorrent.service;

import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentInfo;
import ru.nsu.ooad.proletorrent.dto.TorrentFileTreeNode;

public interface TorrentInfoService {

    TorrentFileTreeNode getTorrentFileStructure(TorrentInfo torrent);

}
