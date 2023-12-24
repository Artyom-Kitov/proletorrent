package ru.nsu.ooad.proletorrent.service;

import org.apache.commons.codec.DecoderException;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentInfo;
import ru.nsu.ooad.proletorrent.dto.TorrentFileTreeNode;
import ru.nsu.ooad.proletorrent.dto.TorrentStatusResponse;
import ru.nsu.ooad.proletorrent.exception.NoSuchTorrentException;
import ru.nsu.ooad.proletorrent.exception.TorrentException;

import java.io.FileNotFoundException;
import java.util.List;

public interface TorrentService {

    long getTorrentSize(String name);

    List<TorrentStatusResponse> getStatuses();

    TorrentFileTreeNode getTorrentFileStructure(TorrentInfo torrent);

    void startUpload(TorrentInfo metaInfo) throws TorrentException;

    StreamingResponseBody download(String name) throws NoSuchTorrentException, FileNotFoundException;

}
