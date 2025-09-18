package com.s3NSE.service;
import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    String uploadFile(MultipartFile file, String folderPath);
    byte[] downloadFile(String fileName);
    String deleteFile(String fileName);
}