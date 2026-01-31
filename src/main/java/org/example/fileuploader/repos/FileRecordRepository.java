package org.example.fileuploader.repos;

import org.example.fileuploader.models.FileRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FileRecordRepository extends JpaRepository<FileRecord, UUID> {
    Optional<FileRecord> findByClientIdAndIdempotencyKey(
            String clientId,
            String idempotencyKey
    );
}
