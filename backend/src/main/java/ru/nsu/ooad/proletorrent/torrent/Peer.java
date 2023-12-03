package ru.nsu.ooad.proletorrent.torrent;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public record Peer(InetSocketAddress address) {

    public Peer(InetAddress address, int port) {
        this(new InetSocketAddress(address, port));
    }

}
