package ru.etu.controlservice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.etu.controlservice.exceptions.FileUnreachableException;
import ru.etu.controlservice.exceptions.S3OperationException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;

@Repository
@RequiredArgsConstructor
public class S3Repository {
    private final S3Client s3Client;
    @Value("${s3.bucket-name}")
    private String BUCKET_NAME;

    public void saveFile(String pathToSave, InputStream fileInputStream) {
        try {
            s3Client.putObject(PutObjectRequest.builder().bucket(BUCKET_NAME).key(pathToSave).build(),
                    RequestBody.fromInputStream(fileInputStream, fileInputStream.available()));
        } catch (IOException e) {
            throw new S3OperationException(e);
        }
    }

    public void deleteFile(String filePath) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(filePath)
                .build());
    }

    public InputStream getFile(String filePath) {
        try {
            return s3Client.getObject(GetObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(filePath)
                    .build());
        } catch (Exception e) {
            throw new FileUnreachableException("Error: file unreachable");
        }
    }
}
