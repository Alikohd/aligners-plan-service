package ru.etu.controlservice.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserFolderUtils {
    private final String PATIENT_FILES_PATTERN_NESTED = "patient-%d-files/case-%d/%s";

    public String addPatientFolder(Long patientId, Long caseId, String fileId) {
        return String.format(PATIENT_FILES_PATTERN_NESTED, patientId, caseId, fileId);
    }
}
