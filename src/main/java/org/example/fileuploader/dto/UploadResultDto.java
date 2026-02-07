package org.example.fileuploader.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UploadResultDto {
    private final String externalId;
    private final String url;
    private final long size;
}
