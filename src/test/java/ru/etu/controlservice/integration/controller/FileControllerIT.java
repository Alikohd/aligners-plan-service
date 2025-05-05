package ru.etu.controlservice.integration.controller;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.etu.controlservice.entity.File;
import ru.etu.controlservice.integration.TestContainersConfig;
import ru.etu.controlservice.repository.FileRepository;
import ru.etu.controlservice.service.BlobService;
import ru.etu.controlservice.service.PatientService;
import ru.etu.controlservice.service.TreatmentCaseService;
import ru.etu.controlservice.util.SegmentationTestData;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@AutoConfigureMockMvc
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FileControllerIT extends TestContainersConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientService patientService;

    @Autowired
    private TreatmentCaseService treatmentCaseService;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private BlobService blobService;

    @Test
    void getFile_ShouldReturnFileResource_WhenFileExists() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        MockMultipartFile testFile = SegmentationTestData.getStlFile("upperJaw.stl");
        String fileUri = blobService.saveFile(testFile, patientId, caseId);
        File file = File.fromS3(fileUri);
        File persistedFile = fileRepository.save(file);

        mockMvc.perform(get("/files/{fileId}", persistedFile.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment;"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, testFile.getBytes().length))
                .andExpect(content().bytes(testFile.getBytes()));
        ;
    }

    @Test
    void getFile_ShouldReturnFileResource_WhenFileNotExists() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        MockMultipartFile testFile = SegmentationTestData.getStlFile("upperJaw.stl");
        String fileUri = blobService.saveFile(testFile, patientId, caseId);
        File file = File.fromS3(fileUri);
        File persistedFile = fileRepository.save(file);

        mockMvc.perform(get("/files/{fileId}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}