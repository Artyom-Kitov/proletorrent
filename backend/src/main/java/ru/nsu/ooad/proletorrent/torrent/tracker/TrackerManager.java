package ru.nsu.ooad.proletorrent.torrent.tracker;

import ru.nsu.ooad.proletorrent.exception.TrackerException;

import java.io.IOException;
import java.net.InetAddress;

public interface TrackerManager {

    AnnounceResponse send(AnnounceRequest request) throws IOException, TrackerException;
    InetAddress getHost();

}
