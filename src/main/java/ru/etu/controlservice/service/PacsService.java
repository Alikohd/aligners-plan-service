package ru.etu.controlservice.service;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import ru.etu.controlservice.dto.DicomResponse;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class PacsService {

    private final RestClient restClient;
    @Value("${pacs.address.base}")
    private String pacsBase;

    @Autowired
    public PacsService(RestClient restClient) {
        this.restClient = restClient;
    }

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

    public DicomResponse sendInstance(MultipartFile file) {

        try {
            String response = restClient.post()
                    .uri(pacsBase + "/instances")
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(file.getBytes())
                    .retrieve()
                    .body(String.class);
            return new Gson().fromJson(response, DicomResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File getInstance(String id) {
        return restClient.get()
                .uri(pacsBase + "/instances/" + id + "/file")
                .retrieve()
                .body(File.class);
    }

}
