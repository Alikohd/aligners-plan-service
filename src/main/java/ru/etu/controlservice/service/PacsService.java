package ru.etu.controlservice.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import ru.etu.controlservice.dto.DicomDto;
import ru.etu.controlservice.dto.DicomResponse;
import ru.etu.controlservice.dto.PacsZipCreationRequestDto;
import ru.etu.controlservice.dto.TreatmentCaseDto;
import ru.etu.controlservice.entity.CtSegmentation;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.Patient;
import ru.etu.controlservice.entity.TreatmentCase;
import ru.etu.controlservice.exceptions.PacsOperationException;
import ru.etu.controlservice.repository.AlignmentSegRepository;
import ru.etu.controlservice.repository.CtSegRepository;
import ru.etu.controlservice.repository.PatientRepository;
import ru.etu.controlservice.repository.TreatmentCaseRepository;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PacsService {

    private final RestClient restClient;

    private final TreatmentCaseRepository treatmentCaseService;

    private final PatientRepository patientRepository;

    private final CtSegRepository ctSegRepository;

    private final NodeService nodeService;

    private final TreatmentCaseRepository treatmentCaseRepository;

    @Value("${pacs.address.base}")
    private String pacsBase;

    public void createCase(){
        Patient patient = patientRepository.save(Patient.builder()
                .build());
        treatmentCaseRepository.save(TreatmentCase.builder()
                        .patient(patient)
                .build());

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

    public List<DicomDto> sendInstance(MultipartFile file, TreatmentCaseDto treatmentCaseDto) {
        List<DicomDto> responses = new ArrayList<>();
        TreatmentCase treatmentCase = treatmentCaseRepository.findById(treatmentCaseDto.id())
                .orElseThrow(() -> new PacsOperationException("TreatmentCase not found"));
        try {
            String response = restClient.post()
                    .uri(pacsBase + "/instances")
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(file.getBytes())
                    .retrieve()
                    .body(String.class);
            Type listType = new TypeToken<ArrayList<DicomDto>>(){}.getType();
            responses.addAll(Objects.requireNonNull(new Gson().fromJson(response, listType)));
            if (!responses.isEmpty()){
                //TODO fix node creation
                //Node node = nodeService.createStep(treatmentCase);
                ctSegRepository.save(
                        CtSegmentation.builder()
                                .ctOriginal(responses.stream().findAny().get().parentSeries())
                                .build()
                );
            }
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
