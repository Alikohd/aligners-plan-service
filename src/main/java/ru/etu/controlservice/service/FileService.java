package ru.etu.controlservice.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.etu.controlservice.dto.FileDto;
import ru.etu.controlservice.repository.S3Repository;
import ru.etu.controlservice.util.UserFolderUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {
    private final S3Repository s3Repository;

    public String saveFile(InputStream file, UUID patientId, UUID caseId) {
        String fileUuid = UUID.randomUUID().toString().replaceAll("-", "");
        String filePath = UserFolderUtils.addPatientFolder(patientId, caseId, fileUuid);

        s3Repository.saveFile(filePath, file);
        return filePath;
    }

    @SneakyThrows
    public FileDto downloadFile(String path) {
        InputStream file = s3Repository.getFile(path);
        String fileName = Paths.get(path).getFileName().toString();
        Resource resource;
        try {
            resource = new ByteArrayResource(file.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error while reading" + fileName + "file", e);
        }
        return new FileDto(fileName, resource);
    }

}
