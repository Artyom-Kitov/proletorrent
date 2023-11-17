package ru.nsu.ooad.proletorrent.service;

import org.apache.commons.codec.DecoderException;
import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentInfo;

public interface TorrentService {

    void startUpload(TorrentInfo metaInfo) throws DecoderException;

}
