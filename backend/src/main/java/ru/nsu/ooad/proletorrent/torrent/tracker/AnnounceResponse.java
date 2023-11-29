package ru.nsu.ooad.proletorrent.torrent.tracker;

import lombok.Builder;
import lombok.Data;
import ru.nsu.ooad.proletorrent.torrent.Peer;

import java.util.List;

@Builder
@Data
public class AnnounceResponse {
    private final int interval;
    private int minInterval;
    private List<Peer> peers;
}
