package ru.etu.controlservice.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;

@Builder
public record PacsZipCreationRequestDto(

        @SerializedName("Asynchronous")
        Boolean asynchronous,

        @SerializedName("Priority")
        Integer priority,

        @SerializedName("Synchronous")
        Boolean synchronous

) {
}
