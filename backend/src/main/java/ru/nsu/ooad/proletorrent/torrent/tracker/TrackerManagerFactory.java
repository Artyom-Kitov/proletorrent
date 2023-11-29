package ru.nsu.ooad.proletorrent.torrent.tracker;

import lombok.experimental.UtilityClass;
import ru.nsu.ooad.proletorrent.exception.TorrentException;
import ru.nsu.ooad.proletorrent.exception.TrackerException;
import ru.nsu.ooad.proletorrent.exception.UnsupportedSchemeException;

import java.net.*;
import java.util.List;
import java.util.Objects;

@UtilityClass
public class TrackerManagerFactory {

    private static TrackerManager getManagerByUrl(URI uri) throws TorrentException {
        if (Objects.equals(uri.getScheme(), "http") || Objects.equals(uri.getScheme(), "https")) {
            return new HttpTrackerManager(uri.toString());
        } else if (Objects.equals(uri.getScheme(), "udp")) {
            try {
                return new UdpTrackerManager(InetAddress.getByName(uri.getHost()), uri.getPort());
            } catch (UnknownHostException e) {
                throw new TrackerException("invalid tracker hostname");
            }
        } else {
            throw new UnsupportedSchemeException("no supported scheme provided");
        }
    }

    public static TrackerManager getByTracker(String announce, List<String> announceList)
            throws TorrentException {
        URI uri;
        if (announce != null) {
            try {
                uri = new URI(announce);
                return getManagerByUrl(uri);
            } catch (URISyntaxException e) {
                throw new TrackerException("invalid announce url");
            }
        }
        if (announceList == null || announceList.isEmpty()) {
            throw new TrackerException("no tracker provided");
        }
        for (String tracker : announceList) {
            try {
                uri = new URI(tracker);
                return getManagerByUrl(uri);
            } catch (URISyntaxException | TorrentException ignore) {
            }
        }
        throw new TrackerException("no valid tracker provided");
    }

}
