package ru.nsu.ooad.proletorrent.torrent;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

public class TorrentFileWriter implements AutoCloseable {

    private static final Path DOWNLOAD_PATH = Path.of("uploads");

    private final String name;
    private final int pieceSize;
    private RandomAccessFile destination;

    public TorrentFileWriter(String name, int pieceSize) throws IOException {
        this.name = name;
        this.pieceSize = pieceSize;

        resolveDownloadFile();
        destination = new RandomAccessFile(DOWNLOAD_PATH.resolve(Path.of(name)).toFile(), "rw");
    }

    public void resolveDownloadFile() throws IOException {
        Path filePath = DOWNLOAD_PATH.resolve(Path.of(name));
        if (!Files.exists(DOWNLOAD_PATH)) {
            Files.createDirectory(DOWNLOAD_PATH);
        } else if (!Files.isDirectory(DOWNLOAD_PATH)) {
            Files.delete(DOWNLOAD_PATH);
            Files.createDirectory(DOWNLOAD_PATH);
        }
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        Files.createFile(filePath);
    }

    public void write(byte[] data, int pieceIndex) throws IOException {
        destination.seek((long) pieceIndex * pieceSize);
        destination.write(data);
    }

    @Override
    public void close() throws IOException {
        destination.close();
    }
}
