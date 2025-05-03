package ru.etu.controlservice.integration.controller;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.etu.controlservice.integration.TestContainersConfig;
import ru.etu.controlservice.service.PatientService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PatientControllerIT extends TestContainersConfig {
    @Autowired
    private PatientService patientService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void addPatient_ShouldSaveAndReturnPatient() throws Exception {
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").isNotEmpty());
    }

    @Test
    void getPatients_ShouldReturnAllPatients() throws Exception {
        patientService.addPatient();
        patientService.addPatient();

        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}