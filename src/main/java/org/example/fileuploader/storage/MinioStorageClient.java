package org.example.fileuploader.storage;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.example.fileuploader.dto.UploadResultDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Component
@Primary
@Slf4j
public class MinioStorageClient implements ExternalFileStorageClient {

    private final MinioClient minioClient;
    private final String bucket;
    private final String publicBaseUrl;

    public MinioStorageClient(
            @Value("${storage.minio.endpoint}") String endpoint,
            @Value("${storage.minio.bucket}") String bucket,
            @Value("${storage.minio.access-key}") String accessKey,
            @Value("${storage.minio.secret-key}") String secretKey,
            @Value("${storage.minio.public-base-url}") String publicBaseUrl
    ) {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        this.bucket = bucket;
        this.publicBaseUrl = publicBaseUrl;
    }

    @Override
    public UploadResultDto upload(MultipartFile file) throws Exception {
        String objectName = file.getOriginalFilename();

        if (objectName == null || objectName.isBlank()) {
            objectName = UUID.randomUUID().toString();
            log.warn("Original filename is null/blank, generated objectName={}", objectName);
        }

        log.info("Uploading file to MinIO. bucket={}, objectName={}", bucket, objectName);

        try (InputStream is = file.getInputStream()) {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName) // <-- кладём в MinIO под этим именем
                    .stream(is, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build();

            minioClient.putObject(args);
        }

        String externalId = objectName;
        String url = String.format("%s/%s/%s", publicBaseUrl, bucket, externalId);

        return new UploadResultDto(externalId, url, file.getSize());
    }

    @Override
    public void delete(String externalId) {
        try {
            RemoveObjectArgs args = RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(externalId)
                    .build();

            minioClient.removeObject(args);
            log.info("Deleted object from MinIO: bucket={}, object={}", bucket, externalId);
        } catch (Exception e) {
            log.warn("Failed to delete object from MinIO: bucket={}, object={}", bucket, externalId, e);
        }
    }
}
