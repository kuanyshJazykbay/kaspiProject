package org.example.fileuploader.service;

import org.example.fileuploader.models.FileRecord;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

public interface FileUploadService {
    CompletableFuture<FileRecord> uploadAndSave(MultipartFile file, String clientId, String idempotencyKey);
}
