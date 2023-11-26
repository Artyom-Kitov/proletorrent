package ru.nsu.ooad.proletorrent.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.nsu.ooad.proletorrent.dto.MessageResponse;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TorrentExistsException.class)
    public ResponseEntity<MessageResponse> torrentExistsException(TorrentExistsException e) {
        log.error(e.getMessage());
        return ResponseEntity.badRequest()
                .body(new MessageResponse("torrent already exists"));
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<MessageResponse> anyException(Throwable e) {
        log.error(e.getMessage());
        e.printStackTrace();
        return ResponseEntity.internalServerError()
                .body(new MessageResponse("something went wrong, please report this problem to the developers"));
    }

}
