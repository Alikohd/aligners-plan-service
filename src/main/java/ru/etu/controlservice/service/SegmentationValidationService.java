package ru.etu.controlservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Slf4j
public class SegmentationValidationService {
    // Valid MIME types for STL files
    private static final List<String> VALID_STL_MIME_TYPES = Arrays.asList(
            "model/stl",
            "application/octet-stream",
            "application/vnd.ms-pki.stl"
    );

    /**
     * Validates that the uploaded file is a ZIP archive containing only .dcm files,
     * allowing nested folders.
     *
     * @param ctArchive the uploaded ZIP file
     * @throws IllegalArgumentException if the file is not a ZIP archive, contains non-.dcm files,
     *                                  or has no .dcm files
     */
    public void validateCtArchive(MultipartFile ctArchive) {
        // Check if the file is a ZIP archive
        if (!ctArchive.getOriginalFilename().endsWith(".zip")) {
            throw new IllegalArgumentException("The uploaded file is not a ZIP archive.");
        }

        try (ZipInputStream zipInputStream = new ZipInputStream(ctArchive.getInputStream())) {
            ZipEntry entry;
            boolean hasDicomFiles = false; // Flag to track if any .dcm files are found

            // Iterate through ZIP entries
            while ((entry = zipInputStream.getNextEntry()) != null) {
                // Skip directories
                if (entry.isDirectory()) {
                    continue;
                }

                // Check if the file has .dcm extension
                if (!entry.getName().toLowerCase().endsWith(".dcm")) {
                    log.warn("The ZIP archive contains non-DICOM files: {}", entry.getName());
                }

                hasDicomFiles = true; // Mark that a .dcm file was found
            }

            // Ensure at least one .dcm file exists in the archive
            if (!hasDicomFiles) {
                throw new IllegalArgumentException("The ZIP archive does not contain any DICOM files.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid ZIP archive or contents.", e);
        }
    }


    /**
     * Validates that the uploaded STL files for upper and lower jaw are non-empty,
     * have the .stl extension, and have a valid MIME type.
     *
     * @param jawUpperStl the STL file for the upper jaw
     * @param jawLowerStl the STL file for the lower jaw
     * @throws IllegalArgumentException if any file is invalid
     */
    public void validateStlFiles(MultipartFile jawUpperStl, MultipartFile jawLowerStl) {
        // Validate both files
        validateSingleStlFile(jawUpperStl, "Upper jaw STL file");
        validateSingleStlFile(jawLowerStl, "Lower jaw STL file");
    }

    /**
     * Validates a single STL file.
     *
     * @param file     the STL file to validate
     * @param fileDesc description of the file (e.g., "Upper jaw STL file")
     * @throws IllegalArgumentException if the file is invalid
     */
    private void validateSingleStlFile(MultipartFile file, String fileDesc) {
        // Check if file is null or empty
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(fileDesc + " is missing or empty.");
        }

        // Check file extension
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".stl")) {
            throw new IllegalArgumentException(fileDesc + " must have a .stl extension.");
        }

        // Check MIME type
        String mimeType = file.getContentType();
        if (mimeType == null || !VALID_STL_MIME_TYPES.contains(mimeType)) {
            throw new IllegalArgumentException(fileDesc + " has an invalid MIME type: " + mimeType);
        }
    }
}