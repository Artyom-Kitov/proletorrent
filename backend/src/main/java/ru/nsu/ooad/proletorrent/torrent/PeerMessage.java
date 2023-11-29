package ru.nsu.ooad.proletorrent.torrent;

import lombok.*;

import java.nio.ByteBuffer;
import java.util.Arrays;

@Builder
public record PeerMessage(Type type, byte[] payload) {

    @Getter
    @RequiredArgsConstructor
    public enum Type {

        KEEP_ALIVE((byte) -1),
        CHOKE((byte) 0),
        UNCHOKE((byte) 1),
        INTERESTED((byte) 2),
        NOT_INTERESTED((byte) 3),
        HAVE((byte) 4),
        BITFIELD((byte) 5),
        REQUEST((byte) 6),
        PIECE((byte) 7),
        CANCEL((byte) 8);

        private final byte id;

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
        if (length == 0) {
            return PeerMessage.builder()
                    .type(Type.KEEP_ALIVE)
                    .build();
        }
        if ((int) (length - 1) < 0) {
            throw new IllegalArgumentException("invalid message format");
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

    public ByteBuffer toByteBuffer() {
        if (type == Type.KEEP_ALIVE) {
            return ByteBuffer.wrap(new byte[]{0});
        }
        int length = payload == null ? 0 : payload.length;
        length++;
        ByteBuffer result = ByteBuffer.allocate(length + 4);
        result.putInt(length);
        result.put(type.getId());
        for (int i = 0; i < length - 1; i++) {
            result.put(payload[i]);
        }
        result.flip();
        return result;
    }

    @Override
    public String toString() {
        return "PeerMessage[type=" + type + ", payload=" + Arrays.toString(payload) + "]";
    }

}
