package ru.nsu.ooad.proletorrent.dto;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TorrentStatusResponse {

    private String id;

    private String name;

    private long size;

    private double progress;

    private int status;

    private Instant createdAt;

    @Getter
    @RequiredArgsConstructor
    public enum Status {

        DOWNLOADING(0),
        SHARING(1),
        PAUSED(2);

        private final int value;

    }

}
