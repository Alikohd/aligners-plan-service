package ru.etu.controlservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.etu.controlservice.dto.FileDto;
import ru.etu.controlservice.exceptions.S3OperationException;
import ru.etu.controlservice.repository.S3Repository;
import ru.etu.controlservice.util.SegmentationTestData;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlobServiceTest {

    @Mock
    private S3Repository s3Repository;

    @InjectMocks
    private BlobService blobService;

    private UUID patientId;
    private UUID caseId;

    @BeforeEach
    void setup() {
        patientId = UUID.randomUUID();
        caseId = UUID.randomUUID();
    }

    @Test
    void saveFile_success() {
        byte[] content = "file content".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", content);

        String savedPath = blobService.saveFile(file, patientId, caseId);

        assertNotNull(savedPath);
        assertTrue(savedPath.contains(patientId.toString()));
        verify(s3Repository).saveFile(eq(savedPath), any(InputStream.class));
    }

    @Test
    void saveFile_throwsOnIOException() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenThrow(new IOException("read error"));

        S3OperationException ex = assertThrows(S3OperationException.class, () ->
                blobService.saveFile(file, patientId, caseId));
        assertTrue(ex.getMessage().contains("Error occurred when reading file"));
    }

    @Test
    void downloadFile_success() {
        String path = SegmentationTestData.mockJawLowerUri;
        byte[] content = "abc123".getBytes();
        InputStream inputStream = new ByteArrayInputStream(content);
        String fileName = Paths.get(path).getFileName().toString();

        when(s3Repository.getFile(path)).thenReturn(inputStream);

        FileDto result = blobService.downloadFile(path);

        assertEquals(fileName, result.name());
        assertNotNull(result.content());
        assertDoesNotThrow(() -> result.content().getInputStream());
    }

    @Test
    void downloadFile_throwsOnIOException() throws IOException {
        String path = SegmentationTestData.mockJawLowerUri;
        InputStream inputStream = mock(InputStream.class);
        when(inputStream.readAllBytes()).thenThrow(new IOException("read error"));
        when(s3Repository.getFile(path)).thenReturn(inputStream);
        String fileName = Paths.get(path).getFileName().toString();

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                blobService.downloadFile(path));
        assertTrue(ex.getMessage().contains("Error while reading " + fileName + " file"));
    }
}
