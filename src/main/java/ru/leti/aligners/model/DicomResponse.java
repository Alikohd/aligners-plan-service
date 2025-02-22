package ru.leti.aligners.model;

import com.google.gson.annotations.SerializedName;
import lombok.*;


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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentPatient() {
        return parentPatient;
    }

    public void setParentPatient(String parentPatient) {
        this.parentPatient = parentPatient;
    }

    public String getParentSeries() {
        return parentSeries;
    }

    public void setParentSeries(String parentSeries) {
        this.parentSeries = parentSeries;
    }

    public String getParentStudy() {
        return parentStudy;
    }

    public void setParentStudy(String parentStudy) {
        this.parentStudy = parentStudy;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
