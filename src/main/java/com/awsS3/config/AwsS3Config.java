package com.awsS3.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * AWS S3 Configuration Class
 *
 * This class sets up the AWS SDK clients (S3Client and S3Presigner)
 * for interacting with Amazon S3 from our Spring Boot application.
 * It reads AWS credentials and region from application.properties.
 *
 * Responsibilities:
 * - Provide S3Client bean for direct operations (upload, list, delete)
 * - Provide S3Presigner bean for generating secure pre-signed URLs
 */
@Configuration
@Slf4j
public class AwsS3Config {

    // AWS Access Key (provided via application.properties or env variables)
    @Value("${aws.s3.access-key}")
    private String accessKey;

    // AWS Secret Key
    @Value("${aws.s3.secret-key}")
    private String secretKey;

    // AWS Region (e.g., ap-south-1 for Mumbai)
    @Value("${aws.s3.region}")
    private String region;

    /**
     * Creates an S3Client bean.
     *
     * S3Client is used for direct synchronous interactions with S3,
     * such as uploading files, listing objects, or deleting them.
     *
     * @return configured S3Client instance
     */
    @Bean
    public S3Client s3Client() {
        log.info("Initializing S3Client for region: {}", region);

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))
                )
                .build();
    }

    /**
     * Creates an S3Presigner bean.
     *
     * S3Presigner is used to generate pre-signed URLs.
     * Pre-signed URLs allow clients (like frontend apps) to upload or
     * download files directly from S3 without needing AWS credentials.
     *
     * @return configured S3Presigner instance
     */
    @Bean
    public S3Presigner s3Presigner() {
        log.info("Initializing S3Presigner for region: {}", region);

        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))
                )
                .build();
    }
}
