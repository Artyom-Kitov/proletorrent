package ru.nsu.ooad.proletorrent.bencode;

import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentInfo;
import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentParser;

import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        TorrentInfo t = TorrentParser.parseTorrent(/*вставить путь*/);
        System.out.println(t);
    }
}
