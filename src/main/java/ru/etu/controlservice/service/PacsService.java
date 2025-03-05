package ru.etu.controlservice.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import ru.etu.controlservice.dto.DicomDto;
import ru.etu.controlservice.dto.PacsZipCreationDto;
import ru.etu.controlservice.dto.PacsZipCreationRequestDto;
import ru.etu.controlservice.exceptions.PacsOperationException;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class PacsService {

    private final RestClient restClient;

    @Value("${pacs.address.base}")
    private String pacsBase;

    public List<String> getPatientsIds() {
        return restClient.get()
                .uri(pacsBase + "/patients")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public List<String> getInstancesIds() {
        return restClient.get()
                .uri(pacsBase + "/instances")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public List<DicomDto> sendInstance(MultipartFile file) {
        List<DicomDto> responses = new ArrayList<>();
        try {
            String response = restClient.post()
                    .uri(pacsBase + "/instances")
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(file.getBytes())
                    .retrieve()
                    .body(String.class);
            Type listType = new TypeToken<ArrayList<DicomDto>>(){}.getType();
            responses.addAll(new Gson().fromJson(response, listType));
            //TODO: add study ID to table
            return responses;
        } catch (IOException e) {
            throw new PacsOperationException("Can't send request to PACS server: " + file.getOriginalFilename() + " was not uploaded", e);
        }
    }

    public File getInstance(String id) {
        return restClient.get()
                .uri(pacsBase + "/instances/" + id + "/file")
                .retrieve()
                .body(File.class);
    }

    public byte[] getZippedSeries(String id){
        String file = restClient.post()
                .uri(pacsBase + "/series/" + id + "/archive")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new Gson().toJson(PacsZipCreationRequestDto.builder()
                        .asynchronous(false)
                        .priority(0)
                        .synchronous(true)
                        .build()))
                .retrieve()
                .body(String.class);
        return file.getBytes();
    }

}
