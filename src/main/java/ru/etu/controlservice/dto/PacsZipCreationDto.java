package ru.etu.controlservice.dto;

import com.google.gson.annotations.SerializedName;

public record PacsZipCreationDto(

        @SerializedName("ID")
        String id,

        @SerializedName("Path")
        String path

) {
}
