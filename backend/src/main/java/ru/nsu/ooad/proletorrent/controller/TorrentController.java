package ru.nsu.ooad.proletorrent.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentFile;
import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentInfo;
import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentParser;
import ru.nsu.ooad.proletorrent.dto.FileInfoResponse;
import ru.nsu.ooad.proletorrent.dto.TorrentFileTreeNode;
import ru.nsu.ooad.proletorrent.service.TorrentInfoService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TorrentController {

    private final TorrentInfoService torrentInfoService;

    @PostMapping("/info")
    public ResponseEntity<TorrentFileTreeNode> getTorrentFileInfo(@RequestParam("torrent") MultipartFile torrent) throws IOException {
        TorrentInfo torrentInfo = TorrentParser.parseTorrent(torrent.getInputStream());
        return ResponseEntity.ok(torrentInfoService.getTorrentFileStructure(torrentInfo));
    }

}
