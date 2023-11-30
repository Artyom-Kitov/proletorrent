package ru.nsu.ooad.proletorrent.torrent.utils;

public class CircularByteBuffer {

    private final byte[] buffer;
    private final int capacity;

    private int readPosition;
    private int writePosition;

    public CircularByteBuffer(int capacity) {
        this.capacity = capacity;
        buffer = new byte[capacity];
    }

    public CircularByteBuffer put(byte b) {
        buffer[writePosition++] = b;
        writePosition %= capacity;
        return this;
    }

    public CircularByteBuffer put(byte[] bytes, int offset, int length) {
        for (int i = 0; i < length; i++) {
            put(bytes[offset + i]);
        }
        return this;
    }

    public byte get() {
        if (readPosition == writePosition) {
            throw new OutOfBytesException("no bytes to get");
        }
        byte b = buffer[readPosition++];
        readPosition %= capacity;
        return b;
    }

    public void get(byte[] dst, int offset, int length) {
        if (bytesAvailable() < length) {
            throw new OutOfBytesException("not enough bytes to get");
        }
        for (int i = 0; i < length; i++) {
            dst[offset + i] = get();
        }
    }

    public byte getWithoutSkip() {
        return buffer[readPosition];
    }

    public void getWithoutSkip(byte[] dst, int offset, int length) {
        if (bytesAvailable() < length) {
            throw new OutOfBytesException("not enough bytes to get");
        }
        if (length >= 0) {
            System.arraycopy(buffer, readPosition, dst, offset, length);
        }
    }

    public int bytesAvailable() {
        int start = readPosition;
        int end = writePosition;
        if (end < readPosition) {
            end += capacity;
        }
        return end - start;
    }

}
