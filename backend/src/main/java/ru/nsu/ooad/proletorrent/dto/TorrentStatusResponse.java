package ru.nsu.ooad.proletorrent.dto;

import lombok.*;

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

    @Getter
    @RequiredArgsConstructor
    public enum Status {

        DOWNLOADING(0),
        SHARING(1),
        PAUSED(2);

        private final int value;

    }

}
