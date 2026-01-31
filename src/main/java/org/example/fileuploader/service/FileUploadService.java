package org.example.fileuploader.service;

import org.example.fileuploader.models.FileRecord;
import org.example.fileuploader.repos.FileRecordRepository;
import org.example.fileuploader.storage.ExternalFileStorageClient;
import org.example.fileuploader.storage.ExternalFileStorageClient.UploadResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class FileUploadService {

    private final ExternalFileStorageClient storage;
    private final FileRecordRepository repository;

    public FileUploadService(
            ExternalFileStorageClient storage,
            FileRecordRepository repository
    ) {
        this.storage = storage;
        this.repository = repository;
    }

    @Async
    @Transactional
    public CompletableFuture<FileRecord> uploadAndSave(
            MultipartFile file,
            String clientId,
            String idempotencyKey
    ) {

        // 1. Идемпотентность: проверяем, возможно уже есть запись
        Optional<FileRecord> existing =
                repository.findByClientIdAndIdempotencyKey(clientId, idempotencyKey);

        if (existing.isPresent()) {
            return CompletableFuture.completedFuture(existing.get());
        }

        UploadResult uploadResult = null;

        try {
            // 2. Загрузка файла во внешнее хранилище
            uploadResult = storage.upload(file);

            // 3. Создаем запись в БД (в той же транзакции)
            FileRecord record = new FileRecord();
            record.setClientId(clientId);
            record.setIdempotencyKey(idempotencyKey);
            record.setExternalId(uploadResult.getExternalId());
            record.setFileName(file.getOriginalFilename());
            record.setSize(uploadResult.getSize());

            FileRecord saved = repository.save(record);

            return CompletableFuture.completedFuture(saved);

        } catch (Exception e) {
            // 4. Компенсация: если файл уже был загружен — пробуем удалить
            if (uploadResult != null) {
                storage.delete(uploadResult.getExternalId());
            }
            CompletableFuture<FileRecord> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }
}

