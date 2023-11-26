package ru.nsu.ooad.proletorrent.repository.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "downloaded")
public class DownloadedTorrent {

    @Id
    private String id;

    private String fullPath;

}
