package ru.nsu.ooad.proletorrent.torrent;

import lombok.Builder;
import lombok.Data;

import java.nio.ByteBuffer;

@Data
@Builder
public class PeerAttachment {
    private PeerState state;
    private ByteBuffer buffer;
    private Peer peer;
}
