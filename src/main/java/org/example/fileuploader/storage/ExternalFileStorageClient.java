package org.example.fileuploader.storage;

import org.example.fileuploader.dto.UploadResultDto;
import org.springframework.web.multipart.MultipartFile;

public interface ExternalFileStorageClient {

    UploadResultDto upload(MultipartFile file) throws Exception;

    void delete(String externalId);
}
