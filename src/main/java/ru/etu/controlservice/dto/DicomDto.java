package ru.etu.controlservice.dto;

import com.google.gson.annotations.SerializedName;

public record DicomDto(
        @SerializedName("ID")
        String id,

        @SerializedName("ParentPatient")
        String parentPatient,

        @SerializedName("ParentSeries")
        String parentSeries,

        @SerializedName("ParentStudy")
        String parentStudy,

        @SerializedName("Path")
        String path,

        @SerializedName("Status")
        String status
) {
}
