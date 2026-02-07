package org.example.fileuploader.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class FileUploadResponseDto {
    private UUID id;
    private String externalId;
    private String fileName;
    private long size;
}
