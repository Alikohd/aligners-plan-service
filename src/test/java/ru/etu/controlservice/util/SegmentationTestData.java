package ru.etu.controlservice.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SegmentationTestData {
    private static final ObjectMapper mapper = new ObjectMapper();
    public static final String mockCtOriginalUri = "pacs://patient-1/ct.zip";
    public static final String mockCtMaskUri = "pacs://patient-1/ctMask.zip";
    public static final String mockJawUpperUri = "patient-1/jawUpper.stl";
    public static final String mockJawLowerUri = "patient-1/jawLower.stl";
    public static final String mockGeneralUri = "testUri";
    public static final String mockJawsSegmented = """
            [
                {
                  "tooth": "222222",
                  "rotation": 20.51,
                  "translation": [0.3, -0.1, 0.2]
                },
                {
                  "tooth": "34",
                  "rotation": 5.62,
                  "translation": [-0.2, 0.1, 0.2]
                }
            ]
            """;
    public static final String mockInitTeethMatrices = """
            [
                {
                    "matrix": [
                        [0.7, 0.3, 0.1, 0.2],
                        [0.2, 0.8, 0.4, 0.5],
                        [0.3, 0.2, 0.6, 1.0]
                    ]
                },
                {
                    "matrix": [
                        [0.6, 0.2, 0.3, 0.4],
                        [0.3, 0.9, 0.2, 0.6],
                        [0.4, 0.9, 1.0, 0.7]
                    ]
                }
            ]
            """;

    public static final String mockDesiredTeethMatrices = """
            [
                {
                    "matrix": [
                        [0.6, 0.2, 0.3, 0.4],
                        [0.3, 0.9, 0.2, 0.6],
                        [0.4, 0.9, 1.0, 0.7]
                    ]
                },
                {
                    "matrix": [
                        [0.7, 0.3, 0.1, 0.2],
                        [0.2, 0.8, 0.4, 0.5],
                        [0.3, 0.2, 0.6, 1.0]
                    ]
                }
            ]
            """;

    public static final String mockMatrixGroup = """
                        {
                            "matrices": [
                                [
                                    [0.8521, 0.2285, 0.1355, 0.3302],
                                    [0.1712, 0.7458, 0.2945, 0.4046],
                                    [0.3323, 0.1742, 0.9850, 0.2051],
                                    [0.0000, 0.0313, 0.000, 1.0052]
                                ],
                                [
                                    [0.6596, 0.3584, 0.2894, 0.4625],
                                    [0.2446, 0.8812, 0.1557, 0.5543],
                                    [0.4232, 0.2758, 0.8340, 0.3571],
                                    [0.0309, 0.0000, 0.0622, 1.0000]
                                ]
                            ]
                        }
                        """;
    public static final String mockAttachment = """
                        {
                            "type": "attachment_1",
                            "coordinates": {
                                "x": 1.5521,
                                "y": -0.8201,
                                "z": 0.3453
                            }
                        }
                        """;

    public static final String mockAlignmentStl = "testAlign.stl";

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

    @SneakyThrows
    public static List<JsonNode> getJsonNodes(String stringJson) {
        JsonNode jsonArray = mapper.readTree(stringJson);
        List<JsonNode> jsonList = new java.util.ArrayList<>();
        jsonArray.elements().forEachRemaining(jsonList::add);
        return jsonList;
    }

    @SneakyThrows
    public static JsonNode getJsonNode(String json) {
        try {
            return mapper.readTree(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
