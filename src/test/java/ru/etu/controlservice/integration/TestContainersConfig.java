package ru.etu.controlservice.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class TestContainersConfig {

    @Container
    private static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:17.3-alpine3.21");

    @Container
    private static final MinIOContainer minioContainer =
            new MinIOContainer("minio/minio:latest")
                    .withUserName("minio")
                    .withPassword("minio123");

    @Container
    private static final GenericContainer<?> orthancContainer =
            new GenericContainer<>("jodogne/orthanc")
                    .withExposedPorts(8042)
                    .withEnv("ORTHANC__AUTHENTICATION_ENABLED", "false");


    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        registry.add("s3.credentials.keyId", minioContainer::getUserName);
        registry.add("s3.credentials.keySecret", minioContainer::getPassword);
        registry.add("s3.uri", minioContainer::getS3URL);

        registry.add("pacs.address.base", () ->
                "http://" + orthancContainer.getHost() + ":" + orthancContainer.getMappedPort(8042));
    }
}