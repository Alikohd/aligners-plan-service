package ru.etu.controlservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.etu.controlservice.dto.DicomResponse;
import ru.etu.controlservice.service.PacsService;

import java.util.List;

@Controller
@RequestMapping("/pacs")
public class PacsController {

    private final PacsService pacsService;

    @Autowired
    public PacsController(PacsService pacsService) {
        this.pacsService = pacsService;
    }


    @PostMapping(value = "/series")
    public ResponseEntity<List<DicomResponse>> uploadSeries(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(pacsService.sendInstance(file));
}

    @GetMapping(value = "/series/{id}", produces = "application/zip")
    public ResponseEntity<byte[]> getZippedSeries(@PathVariable("id") String id){
        return ResponseEntity
                .ok()
                .header("Content-Disposition", "attachment; filename=\"files.zip\"")
                .body(pacsService.getZippedSeries(id));
    }
}
