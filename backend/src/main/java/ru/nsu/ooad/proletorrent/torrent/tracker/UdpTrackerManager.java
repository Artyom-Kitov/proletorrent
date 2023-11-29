package ru.nsu.ooad.proletorrent.torrent.tracker;

import lombok.RequiredArgsConstructor;
import ru.nsu.ooad.proletorrent.exception.TrackerException;
import ru.nsu.ooad.proletorrent.torrent.Peer;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class UdpTrackerManager implements TrackerManager {

    private final InetAddress address;
    private final int port;

    @Override
    public AnnounceResponse send(AnnounceRequest request) throws IOException, TrackerException {
        byte[] transactionId = generateTransactionId();
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(3000);
            byte[] connectionId = obtainConnectionId(socket, transactionId);
            byte[] requestBytes = buildRequest(request, transactionId, connectionId);

            DatagramPacket p = new DatagramPacket(requestBytes, requestBytes.length);
            p.setAddress(address);
            p.setPort(port);
            socket.send(p);

            ByteBuffer buffer = ByteBuffer.allocate(2048);
            DatagramPacket response = new DatagramPacket(buffer.array(), buffer.array().length);
            socket.receive(response);

            if (buffer.getInt() != 1) {
                buffer.getLong();
                throw new TrackerException(new String(buffer.array(), 8, response.getLength() - 1));
            }

            buffer.getInt();
            int interval = buffer.getInt();
            int leechers = buffer.getInt();
            int seeders = buffer.getInt();
            List<Peer> peers = new ArrayList<>();
            for (int i = 0; i < leechers + seeders; i++) {
                byte[] addr = new byte[4];
                buffer.get(addr, 0, 4);
                int port = ((0xFF & buffer.get()) << 8) | (0xFF & buffer.get());
                peers.add(new Peer(InetAddress.getByAddress(addr), port));
            }
            return AnnounceResponse.builder()
                    .interval(interval)
                    .peers(peers)
                    .build();
        }
    }

    private byte[] obtainConnectionId(DatagramSocket socket, byte[] transactionId) throws IOException, TrackerException {
        ByteBuffer buffer = ByteBuffer.allocate(128)
                .putLong(0x41727101980L)
                .putInt(0) // connect
                .put(transactionId);

        DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.position());
        packet.setAddress(address);
        packet.setPort(port);
        socket.send(packet);

        buffer.clear();
        DatagramPacket response = new DatagramPacket(buffer.array(), buffer.array().length);
        socket.receive(response);
        if (buffer.getInt() != 0) {
            throw new TrackerException("invalid tracker response");
        }
        buffer.getInt();
        byte[] result = new byte[8];
        buffer.get(result, 0, 8);
        return result;
    }

    private byte[] buildRequest(AnnounceRequest req, byte[] transactionId, byte[] connectionId) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(connectionId)
                .putInt(1) // announce
                .put(transactionId)
                .put(req.getInfoHash())
                .put(req.getPeerId().getBytes())
                .putLong(req.getDownloaded())
                .putLong(req.getLeft())
                .putLong(req.getUploaded())
                .putInt(req.getEvent().getValue())
                .putInt(0)
                .putInt(0)
                .putInt(req.getNumWant())
                .putShort((short) req.getPort());
        return Arrays.copyOfRange(buffer.array(), 0, buffer.position());
    }

    private byte[] generateTransactionId() {
        byte[] result = new byte[4];
        long value = Math.round(Math.random() * Long.MAX_VALUE);
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (0xFF & value);
            value >>= 8;
        }
        return result;
    }

}
