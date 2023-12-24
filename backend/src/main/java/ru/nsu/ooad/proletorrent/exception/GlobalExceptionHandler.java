package ru.nsu.ooad.proletorrent.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.nsu.ooad.proletorrent.dto.MessageResponse;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @Value("${validation.torrent.exists}")
    private String TORRENT_EXISTS;

    @Value("${validation.torrent.invalid}")
    private String TORRENT_INVALID;

    @Value("${validation.tracker.scheme}")
    private String NO_SUPPORTED_SCHEME_PROVIDED;

    @Value("${validation.unknown}")
    private String INTERNAL_SERVER_ERROR;

    @Value("${validation.torrent.missing}")
    private String NO_SUCH_TORRENT;

    @ExceptionHandler(TorrentExistsException.class)
    public ResponseEntity<MessageResponse> torrentExistsException(TorrentExistsException e) {
        log.error(e.getMessage());
        return ResponseEntity.badRequest()
                .body(new MessageResponse(TORRENT_EXISTS));
    }

    @ExceptionHandler(InvalidTorrentException.class)
    public ResponseEntity<MessageResponse> invalidTorrentException(InvalidTorrentException e) {
        log.error(e.getMessage());
        return ResponseEntity.badRequest()
                .body(new MessageResponse(TORRENT_INVALID));
    }

    @ExceptionHandler(UnsupportedSchemeException.class)
    public ResponseEntity<MessageResponse> unsupportedSchemeException(UnsupportedSchemeException e) {
        log.error(e.getMessage());
        return ResponseEntity.badRequest()
                .body(new MessageResponse(NO_SUPPORTED_SCHEME_PROVIDED));
    }

    @ExceptionHandler(NoSuchTorrentException.class)
    public ResponseEntity<MessageResponse> noSuchTorrentException(NoSuchTorrentException e) {
        log.error(e.getMessage());
        return ResponseEntity.badRequest()
                .body(new MessageResponse(NO_SUCH_TORRENT));
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<MessageResponse> anyException(Throwable e) {
        log.error(e.getMessage());
        e.printStackTrace();
        return ResponseEntity.internalServerError()
                .body(new MessageResponse(INTERNAL_SERVER_ERROR));
    }

}
