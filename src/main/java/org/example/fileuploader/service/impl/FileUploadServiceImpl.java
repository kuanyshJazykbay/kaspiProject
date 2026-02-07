package org.example.fileuploader.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fileuploader.dto.UploadResultDto;
import org.example.fileuploader.models.FileRecord;
import org.example.fileuploader.repos.FileRecordRepository;
import org.example.fileuploader.service.FileUploadService;
import org.example.fileuploader.storage.ExternalFileStorageClient;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@AllArgsConstructor
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    private final FileRecordRepository repository;
    private final ExternalFileStorageClient storage;

    @Override
    @Async
    public CompletableFuture<FileRecord> uploadAndSave(MultipartFile file,
                                                       String clientId,
                                                       String idempotencyKey) {
        Optional<FileRecord> existing =
                repository.findByClientIdAndIdempotencyKey(clientId, idempotencyKey);

        if (existing.isPresent()) {
            log.debug("File already uploaded. clientId={}, idempotencyKey={}", clientId, idempotencyKey);
            return CompletableFuture.completedFuture(existing.get());
        }

        UploadResultDto uploadResultDto = null;

        try {
            uploadResultDto = storage.upload(file);

            FileRecord record = new FileRecord();
            record.setClientId(clientId);
            record.setIdempotencyKey(idempotencyKey);
            record.setExternalId(uploadResultDto.getExternalId());
            record.setFileName(file.getOriginalFilename());
            record.setSize(uploadResultDto.getSize());

            FileRecord saved = repository.save(record);
            log.info("File successfully uploaded and saved. clientId={}, idempotencyKey={}, recordId={}",
                    clientId, idempotencyKey, saved.getId());

            return CompletableFuture.completedFuture(saved);

        } catch (DataIntegrityViolationException ex) {
            log.warn("DataIntegrityViolation on (clientId={}, idempotencyKey={}). " +
                            "Possible concurrent upload. Trying to load existing record.",
                    clientId, idempotencyKey, ex);

            if (uploadResultDto != null) {
                try {
                    storage.delete(uploadResultDto.getExternalId());
                } catch (Exception deleteEx) {
                    log.warn("Failed to cleanup object {} after constraint violation",
                            uploadResultDto.getExternalId(), deleteEx);
                }
            }

            return repository.findByClientIdAndIdempotencyKey(clientId, idempotencyKey)
                    .map(CompletableFuture::completedFuture)
                    .orElseGet(() -> {
                        CompletableFuture<FileRecord> failed = new CompletableFuture<>();
                        failed.completeExceptionally(ex);
                        return failed;
                    });

        } catch (Exception e) {
            log.error("Failed to upload and save file. clientId={}, idempotencyKey={}",
                    clientId, idempotencyKey, e);

            if (uploadResultDto != null) {
                try {
                    storage.delete(uploadResultDto.getExternalId());
                } catch (Exception deleteEx) {
                    log.warn("Failed to cleanup object {} after error",
                            uploadResultDto.getExternalId(), deleteEx);
                }
            }

            CompletableFuture<FileRecord> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }
}