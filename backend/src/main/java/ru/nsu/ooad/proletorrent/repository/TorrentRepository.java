package ru.nsu.ooad.proletorrent.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.ooad.proletorrent.repository.document.DownloadedTorrent;

@Repository
public interface TorrentRepository extends MongoRepository<DownloadedTorrent, String> {
}
