package org.example.fileuploader.storage;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class LocalFolderStorageClient implements ExternalFileStorageClient {

    private final Path rootDir = Path.of("uploads");

    @Override
    public UploadResult upload(MultipartFile file) throws Exception {
        if (!Files.exists(rootDir)) {
            Files.createDirectories(rootDir);
        }

        String externalId = UUID.randomUUID().toString();
        String originalName = file.getOriginalFilename();
        String ext = "";

        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf('.'));
        }

        String storedFileName = externalId + ext;
        Path target = rootDir.resolve(storedFileName);

        try (var is = file.getInputStream()) {
            Files.copy(is, target);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }


        String url = "/uploads/" + storedFileName;

        return new UploadResult(externalId, url, file.getSize());
    }

    @Override
    public void delete(String externalId) {
        try {
            var files = Files.list(rootDir)
                    .filter(p -> p.getFileName().toString().startsWith(externalId))
                    .toList();

            for (Path p : files) {
                Files.deleteIfExists(p);
            }
        } catch (IOException e) {
            System.err.println("Failed to delete file for externalId=" + externalId);
        }
    }
}
