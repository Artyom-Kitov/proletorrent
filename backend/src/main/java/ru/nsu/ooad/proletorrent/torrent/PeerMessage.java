package ru.nsu.ooad.proletorrent.torrent;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;
import java.util.Arrays;

@Builder
public record PeerMessage(Type type, byte[] payload) {

    @Getter
    @RequiredArgsConstructor
    public enum Type {

        CHOKE(0),
        UNCHOKE(1),
        INTERESTED(2),
        NOT_INTERESTED(3),
        HAVE(4),
        BITFIELD(5),
        REQUEST(6),
        PIECE(7),
        CANCEL(8);

        private final int id;

        public static Type of(int id) {
            for (Type t : Type.values()) {
                if (t.id == id) {
                    return t;
                }
            }
            throw new IllegalArgumentException("invalid message id");
        }

    }

    public static PeerMessage buildFromByteBuffer(ByteBuffer buffer) {
        long length = 0;
        for (int i = 0; i < 4; i++) {
            length = (length << 8) | (0xFF & buffer.get());
        }
        Type type = Type.of(buffer.get());
        byte[] payload = new byte[(int) (length - 1)];
        for (int i = 0; i < length - 1; i++) {
            payload[i] = buffer.get();
        }
        return PeerMessage.builder()
                .type(type)
                .payload(payload)
                .build();
    }

    @Override
    public String toString() {
        return "PeerMessage[type=" + type + ", payload=" + Arrays.toString(payload) + "]";
    }

}
