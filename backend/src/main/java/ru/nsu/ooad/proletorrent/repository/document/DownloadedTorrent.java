package ru.nsu.ooad.proletorrent.repository.document;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "downloaded")
@Builder
@Getter
public class DownloadedTorrent {

    @Id
    private String id;

    private String name;

    private long size;

    private String parentFolder;

}
