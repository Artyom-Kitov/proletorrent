package ru.nsu.ooad.proletorrent.torrent.tracker;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
public class AnnounceRequest {

    @RequiredArgsConstructor
    @Getter
    public enum RequestEvent {
        NONE("none", 0),
        COMPLETED("completed", 1),
        STARTED("started", 2),
        STOPPED("stopped", 3);

        private final String name;
        private final int value;
    }

    private final byte[] infoHash;
    private final String peerId;
    private final int port;
    private final long uploaded;
    private final long downloaded;
    private final long left;
    private final boolean compact;
    private final boolean noPeerId;
    private final RequestEvent event;
    private int numWant;
    private final String trackerId;

}
