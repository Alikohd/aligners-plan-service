package ru.etu.controlservice.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.etu.controlservice.dto.FileDto;
import ru.etu.controlservice.exceptions.DownloadFileException;
import ru.etu.controlservice.repository.S3Repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class FileService {
    private final S3Repository s3Repository;

    @SneakyThrows
    public FileDto downloadFile(String path) {
        InputStream file = s3Repository.getFile(path);
        String fileName = Paths.get(path).getFileName().toString();
        Resource resource;
        try {
            resource = new ByteArrayResource(file.readAllBytes());
        } catch (IOException e) {
            throw new DownloadFileException("Error while reading" + fileName + "file", e);
        }
        return new FileDto(fileName, resource);
    }

    public void saveFile(String path, InputStream file) {
        s3Repository.saveFile(path, file);
    }


}

//TODO: add user folder to path for file saving
//TODO: add folders for different branches. Branch id comes from SQL DB
