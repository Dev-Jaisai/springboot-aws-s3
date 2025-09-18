package com.s3NSE.controller;
import com.s3NSE.dto.FileUploadResponse;
import com.s3NSE.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final S3Service s3Service;

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folderPath", required = false) String folderPath) {

        log.info("Received file upload request: {} ({} bytes)",
                file.getOriginalFilename(), file.getSize());

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(FileUploadResponse.builder()
                                .success(false)
                                .message("File is empty")
                                .build());
            }

            String fileUrl = s3Service.uploadFile(file, folderPath);

            FileUploadResponse response = FileUploadResponse.builder()
                    .fileName(file.getOriginalFilename())
                    .fileUrl(fileUrl)
                    .fileSize(file.getSize())
                    .message("File uploaded successfully")
                    .success(true)
                    .build();

            log.info("File upload completed: {}", fileUrl);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("File upload failed: {}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(FileUploadResponse.builder()
                            .success(false)
                            .message("Failed to upload file: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileName) {
        log.info("Received download request for file: {}", fileName);

        try {
            byte[] fileContent = s3Service.downloadFile(fileName);

            return ResponseEntity.ok()
                    .header("Content-Type", "application/octet-stream")
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .body(fileContent);

        } catch (Exception e) {
            log.error("File download failed: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileName) {
        log.info("Received delete request for file: {}", fileName);

        try {
            String result = s3Service.deleteFile(fileName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("File deletion failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete file: " + e.getMessage());
        }
    }
}