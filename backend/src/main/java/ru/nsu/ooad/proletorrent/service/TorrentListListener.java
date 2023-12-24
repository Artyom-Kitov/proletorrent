package ru.nsu.ooad.proletorrent.service;

import java.nio.file.Path;
import java.time.Instant;

public interface TorrentListListener {

    void remove(String key);

    void onUpload(String key, String name, long size, Path fullPath, Instant createdAt);

}
