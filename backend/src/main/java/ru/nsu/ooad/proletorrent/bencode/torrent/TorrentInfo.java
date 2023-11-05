package ru.nsu.ooad.proletorrent.bencode.torrent;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class TorrentInfo {
    private String announce;
    private String name;
    private Long pieceLength;
    private byte[] piecesBlob;
    private List<String> pieces;
    private boolean singleFileTorrent;
    private Long totalSize;
    private List<TorrentFile> fileList;
    private String commentary;
    private String author;
    private Date dateOfCreation;
    private List<String> announceList;
    private String infoHash;

    @Override
    public String toString() {
        return "TorrentInfo{" +
                "announce='" + announce + '\'' +
                ", name='" + name + '\'' +
                ", pieceLength=" + pieceLength +
                ", piecesBlob=" + Arrays.toString(piecesBlob) +
                ", pieces=" + pieces +
                ", singleFileTorrent=" + singleFileTorrent +
                ", totalSize=" + totalSize +
                ", fileList=" + fileList +
                ", commentary='" + commentary + '\'' +
                ", author='" + author + '\'' +
                ", dateOfCreation=" + dateOfCreation +
                ", announceList=" + announceList +
                ", infoHash='" + infoHash + '\'' +
                '}';
    }
}
