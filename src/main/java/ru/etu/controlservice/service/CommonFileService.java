package ru.etu.controlservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.etu.controlservice.dto.FileDto;
import ru.etu.controlservice.entity.File;
import ru.etu.controlservice.exceptions.FileNotFoundException;
import ru.etu.controlservice.repository.FileRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommonFileService {
    private final BlobService blobService;
    private final PacsService pacsService;
    private final FileRepository fileRepository;

    public FileDto getFile(UUID fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(String.format("File with id %s not found", fileId)));
        return switch (file.getStorageType()) {
            case S3 -> blobService.downloadFile(file.getUri());
            case PACS -> pacsService.getZippedSeriesAsFileDto(file.getUri());
        };


    }
}
