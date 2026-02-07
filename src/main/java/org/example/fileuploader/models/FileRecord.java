package org.example.fileuploader.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "file_records",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"client_id", "idempotency_key"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class FileRecord {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "size_bytes")
    private long size;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant createdAt = Instant.now();

}