package ru.nsu.ooad.proletorrent.service;

import org.apache.commons.codec.DecoderException;
import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentInfo;
import ru.nsu.ooad.proletorrent.dto.TorrentFileTreeNode;
import ru.nsu.ooad.proletorrent.dto.TorrentStatusResponse;
import ru.nsu.ooad.proletorrent.exception.TorrentException;

import java.util.List;

public interface TorrentService {

    List<TorrentStatusResponse> getStatuses();

    TorrentFileTreeNode getTorrentFileStructure(TorrentInfo torrent);

    void startUpload(TorrentInfo metaInfo) throws TorrentException;

}
