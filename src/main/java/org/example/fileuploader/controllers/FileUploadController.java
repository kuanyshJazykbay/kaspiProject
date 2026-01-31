package org.example.fileuploader.controllers;

import org.example.fileuploader.service.FileUploadService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileUploadService service;

    public FileUploadController(FileUploadService service) {
        this.service = service;
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public CompletableFuture<ResponseEntity<FileUploadResponse>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Client-Id") String clientId,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        if (file.isEmpty()) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest()
                            .body(new FileUploadResponse(
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
                                new FileUploadResponse(
                                        record.getId(),
                                        record.getExternalId(),
                                        record.getFileName(),
                                        record.getSize()
                                )
                        )
                );
    }

    public static class FileUploadResponse {
        private UUID id;
        private String externalId;
        private String fileName;
        private long size;

        public FileUploadResponse(UUID id, String externalId, String fileName, long size) {
            this.id = id;
            this.externalId = externalId;
            this.fileName = fileName;
            this.size = size;
        }

        public UUID getId() {
            return id;
        }

        public String getExternalId() {
            return externalId;
        }

        public String getFileName() {
            return fileName;
        }

        public long getSize() {
            return size;
        }
    }
}

