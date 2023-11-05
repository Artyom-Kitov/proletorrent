package ru.nsu.ooad.proletorrent.bencode.torrent;

import lombok.Getter;

import java.util.List;

@Getter
public class TorrentFile {

    private final Long fileSize;
    private final List<String> fileInsides;

    public TorrentFile(Long fileSize, List<String> fileInsides) {
        this.fileSize = fileSize;
        this.fileInsides = fileInsides;
    }

    @Override
    public String toString() {
        return "File {" + "fileSize: " + fileSize + ", Inside:" + fileInsides + '}';
    }
}
