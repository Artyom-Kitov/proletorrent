package ru.nsu.ooad.proletorrent.torrent.tracker;

import lombok.experimental.UtilityClass;
import ru.nsu.ooad.proletorrent.exception.TorrentException;
import ru.nsu.ooad.proletorrent.exception.TrackerException;
import ru.nsu.ooad.proletorrent.exception.UnsupportedSchemeException;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@UtilityClass
public class TrackerManagerFactory {

    public static List<TrackerManager> of(String announce, List<String> announceList)
            throws TorrentException {
        List<TrackerManager> result = new ArrayList<>();
        URI uri;
        if (announce != null) {
            try {
                uri = new URI(announce);
                result.add(getManagerByUri(uri));
            } catch (URISyntaxException e) {
                throw new TrackerException("invalid announce url");
            }
        }
        if (announceList == null || announceList.isEmpty()) {
            return result;
        }
        for (String tracker : announceList) {
            try {
                uri = new URI(tracker);
                result.add(getManagerByUri(uri));
            } catch (URISyntaxException | TorrentException ignore) {
            }
        }
        return result;
    }

    private static TrackerManager getManagerByUri(URI uri) throws TorrentException {
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

}
