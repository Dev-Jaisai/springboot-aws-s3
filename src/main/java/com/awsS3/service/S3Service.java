package com.awsS3.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for AWS S3 operations.
 * Handles direct uploads, listing files,
 * and generating pre-signed URLs for secure access.
 */
@Service
@Slf4j
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner presigner;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public S3Service(S3Client s3Client, S3Presigner presigner) {
        this.s3Client = s3Client;
        this.presigner = presigner;
    }

    /**
     * Upload a file directly to S3 using the given key.
     */
    public String uploadFile(File file, String keyName) {
        log.info("Uploading file '{}' to bucket '{}'", keyName, bucketName);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build();

        s3Client.putObject(request, RequestBody.fromFile(Paths.get(file.getAbsolutePath())));

        log.info("File '{}' uploaded successfully", keyName);
        return "File uploaded successfully: " + keyName;
    }

    /**
     * Generate a pre-signed URL for uploading a file.
     * The URL is valid for 10 minutes and creates a unique key each time.
     */
    public URL generateUploadUrl(String fileName) {
        String uniqueFileName = "uploads/" + UUID.randomUUID() + "-" + fileName;
        log.info("Generating pre-signed upload URL for file '{}'", uniqueFileName);

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(uniqueFileName)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);

        log.info("Pre-signed upload URL generated for '{}'", uniqueFileName);
        return presignedRequest.url();
    }

    /**
     * Generate a pre-signed URL for downloading a file.
     * The URL is valid for 10 minutes.
     */
    public URL generateDownloadUrl(String fileKey) {
        log.info("Generating pre-signed download URL for file '{}'", fileKey);

        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey) // use the full key directly
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(objectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);

        log.info("Pre-signed download URL generated for '{}'", fileKey);
        return presignedRequest.url();
    }

    /**
     * List all files in the 'uploads/' folder of the bucket.
     */
    public List<String> listFiles() {
        log.info("Listing all files in bucket '{}' under prefix 'uploads/'", bucketName);

        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix("uploads/") // restrict to uploads folder
                .build();

        ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

        List<String> fileKeys = listResponse.contents().stream()
                .map(S3Object::key)
                .collect(Collectors.toList());

        log.info("Found {} files in 'uploads/'", fileKeys.size());
        return fileKeys;
    }
}
