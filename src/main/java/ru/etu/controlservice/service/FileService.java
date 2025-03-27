package ru.etu.controlservice.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.etu.controlservice.dto.FileDto;
import ru.etu.controlservice.exceptions.DownloadFileException;
import ru.etu.controlservice.repository.S3Repository;
import ru.etu.grpc.segmentation.SegmentationServiceGrpc;
import ru.etu.controlservice.util.UserFolderUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    @GrpcClient("JawSegmentationClient")
    private SegmentationServiceGrpc.SegmentationServiceBlockingStub stub;

    private final S3Repository s3Repository;

    public String saveFile(InputStream file, Long patientId, Long caseId) {
        String fileId = UUID.randomUUID().toString().replaceAll("-", "");
        String filePath = UserFolderUtils.addPatientFolder(patientId, caseId, fileId);
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
            throw new DownloadFileException("Error while reading" + fileName + "file", e);
        }
        return new FileDto(fileName, resource);
    }

}

//TODO: add user folder to path for file saving
//TODO: add folders for different branches. Branch id comes from SQL DB
