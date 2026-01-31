package org.example.fileuploader.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ExternalFileStorageClient {

    UploadResult upload(MultipartFile file) throws Exception;
    void delete(String externalId);

    class UploadResult {
        private final String externalId;
        private final String url;
        private final long size;

        public UploadResult(String externalId, String url, long size) {
            this.externalId = externalId;
            this.url = url;
            this.size = size;
        }

        public String getExternalId() {
            return externalId;
        }

        public String getUrl() {
            return url;
        }

        public long getSize() {
            return size;
        }
    }
}
