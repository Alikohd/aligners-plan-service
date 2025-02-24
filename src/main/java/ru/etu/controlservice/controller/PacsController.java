package ru.etu.controlservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.etu.controlservice.dto.DicomDto;
import ru.etu.controlservice.service.PacsService;

import java.io.IOException;
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
    public ResponseEntity<List<DicomDto>> uploadSeries(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(pacsService.sendInstance(file));
    }

    @GetMapping(value = "/series/{id}", produces = "application/zip")
    public ResponseEntity<byte[]> getZippedSeries(@PathVariable("id") String id) throws IOException {
        return ResponseEntity
                .ok()
                .header("Content-Disposition", "attachment; filename=\"files.zip\"")
                .body(pacsService.getZippedSeries(id));
    }
}
