package ru.nsu.ooad.proletorrent.torrent.utils;

import ru.nsu.ooad.proletorrent.torrent.PeerMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class PeerBuffer {

    private final int capacity;

    private final CircularByteBuffer buffer;
    private final ByteBuffer inputBuffer;

    public PeerBuffer(int capacity) {
        this.capacity = capacity;
        buffer = new CircularByteBuffer(capacity);
        inputBuffer = ByteBuffer.allocate(capacity);
    }

    public void readFromChannel(SocketChannel channel) throws IOException {
        inputBuffer.clear();
        int readBytes = channel.read(inputBuffer);
        buffer.put(inputBuffer.array(), 0, readBytes);
    }

    public ByteBuffer getHandshake(int handshakeSize) throws NotEnoughBytesException {
        if (buffer.bytesAvailable() < handshakeSize) {
            throw new NotEnoughBytesException("not enough bytes for handshake");
        }
        byte[] handshake = new byte[handshakeSize];
        buffer.get(handshake, 0, handshakeSize);
        return ByteBuffer.wrap(handshake);
    }

    public List<PeerMessage> getMessages() {
        List<PeerMessage> messages = new ArrayList<>();
        while (true) {
            if (buffer.bytesAvailable() < 4) {
                break;
            }
            byte[] lengthBytes = new byte[4];
            buffer.getWithoutSkip(lengthBytes, 0, 4);
            int length = 0;
            for (byte b : lengthBytes) {
                length = (length << 8) | (0xFF & b);
            }
            if (length == 0) {
                messages.add(PeerMessage.builder()
                        .type(PeerMessage.Type.KEEP_ALIVE)
                        .build());
            }
            if (buffer.bytesAvailable() < length + 4) {
                break;
            }
            byte[] messageBytes = new byte[length + 4];
            buffer.get(messageBytes, 0, length + 4);
            messages.add(PeerMessage.buildFromByteBuffer(ByteBuffer.wrap(messageBytes)));
        }
        return messages;
    }

}
