package com.s3NSE.dto;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileUploadRequest {
    private MultipartFile file;
    private String folderPath; // Optional folder path within bucket
}