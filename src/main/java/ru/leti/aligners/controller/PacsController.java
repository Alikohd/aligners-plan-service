package ru.leti.aligners.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.leti.aligners.model.DicomResponse;
import ru.leti.aligners.service.PacsService;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/pacs")
public class PacsController {

    private final PacsService pacsService;

    @Autowired
    public PacsController(PacsService pacsService) {
        this.pacsService = pacsService;
    }

    @PostMapping("/series")
    public ResponseEntity<List<DicomResponse>> uploadSeries(@RequestParam("files") List<MultipartFile> files){
        List<DicomResponse> list = new ArrayList<>();
        files.forEach(file -> list.add(pacsService.sendInstance(file)));
        return ResponseEntity.ok(list);
    }


}
