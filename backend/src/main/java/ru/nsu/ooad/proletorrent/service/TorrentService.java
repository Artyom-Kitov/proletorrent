package ru.nsu.ooad.proletorrent.service;

import org.apache.commons.codec.DecoderException;
import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentInfo;
import ru.nsu.ooad.proletorrent.dto.TorrentFileTreeNode;

public interface TorrentService {

    TorrentFileTreeNode getTorrentFileStructure(TorrentInfo torrent);

    void startUpload(TorrentInfo metaInfo) throws DecoderException;

}
