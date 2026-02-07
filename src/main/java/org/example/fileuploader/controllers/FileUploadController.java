package org.example.fileuploader.controllers;

import lombok.AllArgsConstructor;
import org.example.fileuploader.dto.FileUploadResponseDto;
import org.example.fileuploader.service.FileUploadService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/files")
@AllArgsConstructor
public class FileUploadController {

    private final FileUploadService service;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public CompletableFuture<ResponseEntity<FileUploadResponseDto>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Client-Id") String clientId,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        if (file.isEmpty()) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest()
                            .body(new FileUploadResponseDto(
                                    null,
                                    null,
                                    null,
                                    0L
                            ))
            );
        }

        return service.uploadAndSave(file, clientId, idempotencyKey)
                .thenApply(record ->
                        ResponseEntity.ok(
                                new FileUploadResponseDto(
                                        record.getId(),
                                        record.getExternalId(),
                                        record.getFileName(),
                                        record.getSize()
                                )
                        )
                );
    }
}

