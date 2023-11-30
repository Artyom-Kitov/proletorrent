package ru.nsu.ooad.proletorrent.torrent;

import lombok.Builder;
import lombok.Data;
import ru.nsu.ooad.proletorrent.torrent.utils.PeerBuffer;

@Data
@Builder
public class PeerAttachment {
    private PeerBuffer buffer;
    private Peer peer;
    private boolean isApproved;
    private boolean isUnchoked;
    private boolean isInterested;
}
