package ru.nsu.ooad.proletorrent.repository;

import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;
import ru.nsu.ooad.proletorrent.repository.document.DownloadedTorrent;

import java.util.Optional;
import java.util.function.Function;

@Repository
public interface TorrentRepository extends MongoRepository<DownloadedTorrent, String> {

    boolean existsByName(String name);

    Optional<DownloadedTorrent> findByName(String name);

}
