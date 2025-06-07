package ru.etu.controlservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import ru.etu.controlservice.dto.FileDto;
import ru.etu.controlservice.entity.File;
import ru.etu.controlservice.entity.StorageType;
import ru.etu.controlservice.exceptions.FileNotFoundException;
import ru.etu.controlservice.repository.FileRepository;
import ru.etu.controlservice.util.SegmentationTestData;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommonFileServiceTest {

    @Mock
    private BlobService blobService;

    @Mock
    private PacsService pacsService;

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private CommonFileService commonFileService;

    @Test
    void getFile_fromS3_success() {
        UUID fileId = UUID.randomUUID();
        String uri = SegmentationTestData.mockJawUpperUri;
        FileDto resource = new FileDto("file.obj", new ByteArrayResource("test".getBytes()));

        File file = new File();
        file.setId(fileId);
        file.setUri(uri);
        file.setStorageType(StorageType.S3);

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(blobService.downloadFile(uri)).thenReturn(new FileDto("file.obj", resource.content()));

        FileDto result = commonFileService.getFile(fileId);
        assertNotNull(result);
        assertEquals(resource, result);
    }

    @Test
    void getFile_fromPACS_success() {
        UUID fileId = UUID.randomUUID();
        String uri = SegmentationTestData.mockGeneralUri;
        FileDto pacsResource = new FileDto("ct.zip", new ByteArrayResource("dicom zip".getBytes()));

        File file = new File();
        file.setId(fileId);
        file.setUri(uri);
        file.setStorageType(StorageType.PACS);

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(pacsService.getZippedSeriesAsFileDto(uri)).thenReturn(pacsResource);

        FileDto result = commonFileService.getFile(fileId);
        assertNotNull(result);
        assertEquals(pacsResource, result);
    }

    @Test
    void getFile_throwsWhenFileNotFound() {
        UUID fileId = UUID.randomUUID();
        when(fileRepository.findById(fileId)).thenReturn(Optional.empty());

        assertThrows(FileNotFoundException.class, () -> commonFileService.getFile(fileId));
    }
}
