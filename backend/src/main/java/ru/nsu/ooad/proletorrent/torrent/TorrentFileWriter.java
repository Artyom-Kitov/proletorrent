package ru.nsu.ooad.proletorrent.torrent;

import lombok.Getter;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

public class TorrentFileWriter implements AutoCloseable {

    private static final Path DOWNLOAD_PATH = Path.of("uploads");

    private final String name;

    @Getter
    private Path filePath;

    private final int pieceSize;
    private final RandomAccessFile destination;

    @Getter
    private int totalDownloaded = 0;

    public TorrentFileWriter(String name, int pieceSize) throws IOException {
        this.name = name;
        this.pieceSize = pieceSize;

        resolveDownloadFile();
        destination = new RandomAccessFile(DOWNLOAD_PATH.resolve(Path.of(name)).toFile(), "rw");
    }

    public void resolveDownloadFile() throws IOException {
        Path torrentPath = DOWNLOAD_PATH.resolve(Path.of(name));
        if (!Files.exists(DOWNLOAD_PATH)) {
            Files.createDirectory(DOWNLOAD_PATH);
        } else if (!Files.isDirectory(DOWNLOAD_PATH)) {
            Files.delete(DOWNLOAD_PATH);
            Files.createDirectory(DOWNLOAD_PATH);
        }
        if (Files.exists(torrentPath)) {
            Files.delete(torrentPath);
        }
        Files.createFile(torrentPath);
        filePath = torrentPath.toAbsolutePath();
    }

    public void write(byte[] data, int pieceIndex) throws IOException {
        destination.seek((long) pieceIndex * pieceSize);
        destination.write(data);
        totalDownloaded += data.length;
    }

    @Override
    public void close() throws IOException {
        destination.close();
    }
}
