package org.example.fileuploader.models;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "file_records",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"client_id", "idempotency_key"}
        )
)
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

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
    public FileRecord() {
    }

    // getters / setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
