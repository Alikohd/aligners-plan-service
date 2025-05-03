package ru.etu.controlservice.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SegmentationTestData {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static MockMultipartFile getDicomArchive() throws IOException {
        ClassPathResource resource = new ClassPathResource("data/testdicom.zip");
        byte[] fileContent = Files.readAllBytes(resource.getFile().toPath());
        return new MockMultipartFile(
                "ctArchive",
                "testdicom.zip",
                "application/zip",
                fileContent
        );
    }

    public static MockMultipartFile getDicomArchiveAmended() throws IOException {
        ClassPathResource resource = new ClassPathResource("data/testdicom.zip");
        byte[] fileContent = Files.readAllBytes(resource.getFile().toPath());
        return new MockMultipartFile(
                "amendedCtMask",
                "testdicom.zip",
                "application/zip",
                fileContent
        );
    }

    public static MockMultipartFile getInvalidDicomArchive() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("not_a_dicom.txt");
            zos.putNextEntry(entry);
            zos.write("This is not a DICOM file".getBytes());
            zos.closeEntry();
        }
        return new MockMultipartFile(
                "ctArchive",
                "invalid_content.zip",
                "application/zip",
                baos.toByteArray()
        );
    }

    public static MockMultipartFile getStlFile(String filename) {
        String stlContent = """
                solid test
                facet normal 0 0 1
                    outer loop
                        vertex 0 0 0
                        vertex 1 0 0
                        vertex 0 1 0
                    endloop
                endfacet
                endsolid test
                """;

        return new MockMultipartFile(
                filename,
                "test.stl",
                "model/stl",
                stlContent.getBytes()
        );
    }

    public static MockMultipartFile getInvalidTextStlFile(String filename) {
        String invalidContent = "This is not an STL file";
        return new MockMultipartFile(
                filename,
                "invalid.stl",
                "model/stl",
                invalidContent.getBytes()
        );
    }

    public static JsonNode getMockJson() {
        return mapper.createObjectNode()
                .put("test", "testdata");
    }

    public static JsonNode getUpdatedMockJson() {
        return mapper.createObjectNode()
                .put("test", "updatedTestData");
    }
}
