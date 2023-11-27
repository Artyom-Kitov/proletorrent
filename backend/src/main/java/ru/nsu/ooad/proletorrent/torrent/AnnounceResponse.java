package ru.nsu.ooad.proletorrent.torrent;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class AnnounceResponse {
    private final int interval;
    private int minInterval;
    private List<Peer> peers;
}
