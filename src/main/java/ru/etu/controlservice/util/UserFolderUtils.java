package ru.etu.controlservice.util;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class UserFolderUtils {
    private final String PATIENT_FILES_PATTERN_NESTED = "patient-%s-files/case-%s/%s";

    public String addPatientFolder(UUID patientId, UUID caseId, String fileId) {
        return String.format(PATIENT_FILES_PATTERN_NESTED, patientId, caseId, fileId);
    }
}
