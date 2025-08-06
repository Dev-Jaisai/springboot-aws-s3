package com.awsS3.controller;

import com.awsS3.service.S3Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * REST Controller for AWS S3 operations.
 * Provides APIs for uploading files, generating pre-signed URLs,
 * and listing files stored in the S3 bucket.
 */
@RestController
@RequestMapping("/s3")
public class S3Controller {

    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    /**
     * Uploads a file directly to S3.
     */
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        // Save to temporary file before upload
        File tempFile = File.createTempFile("upload-", file.getOriginalFilename());
        file.transferTo(tempFile);

        // Store inside uploads/ folder in S3
        String keyName = "uploads/" + file.getOriginalFilename();
        String result = s3Service.uploadFile(tempFile, keyName);

        tempFile.delete(); // clean up temp file
        return result;
    }

    /**
     * Returns a pre-signed URL for uploading a file.
     */
    @GetMapping("/presigned/upload-url")
    public String getUploadUrl(@RequestParam String fileName) {
        return s3Service.generateUploadUrl(fileName).toString();
    }

    /**
     * Returns a pre-signed URL for downloading a file.
     */
    @GetMapping("/presigned/download-url")
    public String getDownloadUrl(@RequestParam String fileKey) {
        return s3Service.generateDownloadUrl(fileKey).toString();
    }

    /**
     * Lists all files in the S3 uploads folder.
     */
    @GetMapping("/files")
    public List<String> listFiles() {
        return s3Service.listFiles();
    }

    @GetMapping("/demo")
    public String getrDemo(){
        return "Working fine";
    }
}
