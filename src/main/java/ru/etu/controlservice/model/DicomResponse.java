package ru.etu.controlservice.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DicomResponse {

    @SerializedName("ID")
    private String id;

    @SerializedName("ParentPatient")
    private String parentPatient;

    @SerializedName("ParentSeries")
    private String parentSeries;

    @SerializedName("ParentStudy")
    private String parentStudy;

    @SerializedName("Path")
    private String path;

    @SerializedName("Status")
    private String status;

}
