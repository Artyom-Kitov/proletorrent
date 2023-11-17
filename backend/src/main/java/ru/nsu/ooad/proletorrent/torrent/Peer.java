package ru.nsu.ooad.proletorrent.torrent;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

@Getter
@Data
public class Peer {

    private final InetSocketAddress address;

    public Peer(String ip, int port) throws UnknownHostException {
        this.address = new InetSocketAddress(InetAddress.getByName(ip), port);
    }

    public Peer(InetAddress address, int port) {
        this.address = new InetSocketAddress(address, port);
    }

}
