package ru.nsu.ooad.proletorrent.torrent;

import ru.nsu.ooad.proletorrent.exception.TrackerException;

import java.io.IOException;

public interface TrackerManager {

    AnnounceResponse send(AnnounceRequest request) throws IOException, TrackerException;

}
