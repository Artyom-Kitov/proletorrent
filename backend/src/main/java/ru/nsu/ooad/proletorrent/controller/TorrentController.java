package ru.nsu.ooad.proletorrent.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.nsu.ooad.proletorrent.bencode.BencodeException;
import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentInfo;
import ru.nsu.ooad.proletorrent.bencode.torrent.TorrentParser;
import ru.nsu.ooad.proletorrent.dto.TorrentFileTreeNode;
import ru.nsu.ooad.proletorrent.dto.TorrentStatusResponse;
import ru.nsu.ooad.proletorrent.dto.UploadStatusResponse;
import ru.nsu.ooad.proletorrent.exception.InvalidTorrentException;
import ru.nsu.ooad.proletorrent.exception.NoSuchTorrentException;
import ru.nsu.ooad.proletorrent.exception.TorrentException;
import ru.nsu.ooad.proletorrent.service.TorrentService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TorrentController {

    private final TorrentService torrentService;

    @PostMapping("/info")
    public ResponseEntity<TorrentFileTreeNode> getTorrentFileInfo(
            @RequestParam("torrent") MultipartFile torrent) throws InvalidTorrentException {
        TorrentInfo metaInfo;
        try {
            metaInfo = TorrentParser.parseTorrent(torrent.getInputStream());
        } catch (BencodeException | IOException e) {
            throw new InvalidTorrentException(e);
        }
        return ResponseEntity.ok(torrentService.getTorrentFileStructure(metaInfo));
    }

    @PostMapping("/start-upload")
    public ResponseEntity<UploadStatusResponse> startUpload(
            @RequestParam("torrent") MultipartFile torrent) throws TorrentException {
        TorrentInfo metaInfo;
        try {
            metaInfo = TorrentParser.parseTorrent(torrent.getInputStream());
        } catch (BencodeException | IOException e) {
            throw new InvalidTorrentException(e);
        }
        torrentService.startUpload(metaInfo);
        return ResponseEntity.ok(new UploadStatusResponse("upload started"));
    }

    @GetMapping("/statuses")
    public ResponseEntity<List<TorrentStatusResponse>> getStatuses() {
        return ResponseEntity.ok(torrentService.getStatuses());
    }

    @GetMapping("/download/{name}")
    public ResponseEntity<Resource> download(@PathVariable String name) throws NoSuchTorrentException, IOException {
        Resource resource = torrentService.download(name);
        return ResponseEntity.ok()
                .contentLength(resource.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

}
