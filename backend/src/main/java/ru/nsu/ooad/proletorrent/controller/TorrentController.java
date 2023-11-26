package ru.nsu.ooad.proletorrent.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentInfo;
import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentParser;
import ru.nsu.ooad.proletorrent.dto.TorrentFileTreeNode;
import ru.nsu.ooad.proletorrent.dto.UploadStatusResponse;
import ru.nsu.ooad.proletorrent.service.TorrentService;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TorrentController {

    private final TorrentService torrentService;

    @PostMapping("/info")
    public ResponseEntity<TorrentFileTreeNode> getTorrentFileInfo(
            @RequestParam("torrent") MultipartFile torrent) throws IOException {
        TorrentInfo metaInfo = TorrentParser.parseTorrent(torrent.getInputStream());
        return ResponseEntity.ok(torrentService.getTorrentFileStructure(metaInfo));
    }

    @PostMapping("/start-upload")
    public ResponseEntity<UploadStatusResponse> startUpload(
            @RequestParam("torrent") MultipartFile torrent) throws IOException, DecoderException {
        TorrentInfo metaInfo = TorrentParser.parseTorrent(torrent.getInputStream());
        torrentService.startUpload(metaInfo);
        return ResponseEntity.ok(new UploadStatusResponse("upload started"));
    }

}
