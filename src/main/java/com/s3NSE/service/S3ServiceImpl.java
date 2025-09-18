package com.s3NSE.service;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final AmazonS3 s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    @Override
    public String uploadFile(MultipartFile file, String folderPath) {
        try {
            String fileName = generateFileName(file.getOriginalFilename(), folderPath);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName,
                    fileName,
                    file.getInputStream(),
                    metadata
            );

            s3Client.putObject(putObjectRequest);

            String fileUrl = s3Client.getUrl(bucketName, fileName).toString();
            log.info("File uploaded successfully: {} -> {}", fileName, fileUrl);

            return fileUrl;

        } catch (IOException e) {
            log.error("Error uploading file to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }
    @Override
    public byte[] downloadFile(String fileName) {
        try {
            S3Object s3Object = s3Client.getObject(bucketName, fileName);
            S3ObjectInputStream inputStream = s3Object.getObjectContent();
            byte[] content = IOUtils.toByteArray(inputStream);
            inputStream.close();

            log.info("File downloaded successfully: {}", fileName);
            return content;

        } catch (IOException e) {
            log.error("Error downloading file from S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to download file from S3", e);
        }
    }

    @Override
    public String deleteFile(String fileName) {
        try {
            s3Client.deleteObject(bucketName, fileName);
            log.info("File deleted successfully: {}", fileName);
            return "File deleted successfully";

        } catch (Exception e) {
            log.error("Error deleting file from S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete file from S3", e);
        }
    }

    // Other methods remain the same...

    private String generateFileName(String originalFileName, String folderPath) {
        // Remove special characters and spaces from original filename
        String cleanFileName = originalFileName.replaceAll("[^a-zA-Z0-9.-]", "_");

        // Create NSE prefix filename
        String nseFileName = "NSE_" + cleanFileName;

        if (folderPath != null && !folderPath.trim().isEmpty()) {
            // Remove leading/trailing slashes and add proper formatting
            String cleanPath = folderPath.replaceAll("^/+|/+$", "");
            return cleanPath + "/" + nseFileName;
        }

        return nseFileName;
    }}