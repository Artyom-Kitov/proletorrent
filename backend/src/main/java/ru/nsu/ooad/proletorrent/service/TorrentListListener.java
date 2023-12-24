package ru.nsu.ooad.proletorrent.service;

import java.nio.file.Path;

public interface TorrentListListener {

    void remove(String key);

    void onUpload(String key, String name, long size, Path fullPath);

}
