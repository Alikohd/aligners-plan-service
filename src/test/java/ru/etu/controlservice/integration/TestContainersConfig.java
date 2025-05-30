package ru.etu.controlservice.integration;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketConfiguration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.net.URI;

@Testcontainers
public class TestContainersConfig {
    private static final String BUCKET_NAME = "patient-files";

    @Container
    private static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:17.3-alpine3.21");

    @Container
    private static final MinIOContainer minioContainer =
            new MinIOContainer("minio/minio:RELEASE.2025-04-22T22-12-26Z")
                    .withUserName("minio")
                    .withPassword("minio123");

    @Container
    private static final GenericContainer<?> orthancContainer =
            new GenericContainer<>("orthancteam/orthanc:24.3.3")
                    .withExposedPorts(8042)
                    .withEnv("ORTHANC_JSON", """
                            {
                              "AuthenticationEnabled": false
                            }
                            """);

    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        registry.add("s3.credentials.keyId", minioContainer::getUserName);
        registry.add("s3.credentials.keySecret", minioContainer::getPassword);
        registry.add("s3.uri", minioContainer::getS3URL);
        registry.add("S3_BUCKET_NAME", () -> BUCKET_NAME);

        registry.add("pacs.address.base", () ->
                "http://" + orthancContainer.getHost() + ":" + orthancContainer.getMappedPort(8042));
    }

    @BeforeAll
    static void setup() {
        try (S3Client s3Client = S3Client.builder()
                .endpointOverride(URI.create(minioContainer.getS3URL()))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                minioContainer.getUserName(),
                                minioContainer.getPassword()
                        )
                ))
                .forcePathStyle(true)
                .build()) {

            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(BUCKET_NAME)
                    .createBucketConfiguration(CreateBucketConfiguration.builder()
                            .build())
                    .build());
        }
    }
}