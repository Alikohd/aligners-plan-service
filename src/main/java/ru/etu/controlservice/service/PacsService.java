package ru.etu.controlservice.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import ru.etu.controlservice.CtMaskGrpc;
import ru.etu.controlservice.CtMaskServiceProto;
import ru.etu.controlservice.dto.DicomDto;
import ru.etu.controlservice.dto.DicomResponse;
import ru.etu.controlservice.dto.PacsZipCreationRequestDto;
import ru.etu.controlservice.exceptions.PacsOperationException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PacsService {

    @GrpcClient("CtMaskClient")
    private CtMaskGrpc.CtMaskBlockingStub ctMaskBlockingStub;

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
            responses.addAll(Objects.requireNonNull(new Gson().fromJson(response, listType)));
            if (!responses.isEmpty()) {
                String maskUrl = sendToPlug(pacsBase, responses.stream().findAny().get().parentSeries());
                //TODO: add mask series ID to table
            }
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

    private String sendToPlug(String pacsBase, String seriesId){
        CtMaskServiceProto.DicomRequest request = CtMaskServiceProto.DicomRequest.newBuilder()
                .setPacsBase(pacsBase)
                .setSeriesId(seriesId)
                .build();

        CtMaskServiceProto.DicomReply reply = ctMaskBlockingStub.sendDicomUrl(request);

        return reply.getSeriesId();
    }

}
