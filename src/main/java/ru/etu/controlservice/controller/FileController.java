package ru.etu.controlservice.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.etu.controlservice.dto.FileDto;
import ru.etu.controlservice.service.FileService;

import java.io.IOException;

@RestController
@RequestMapping("api/storage/s3")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("download")
    @ResponseStatus
    public ResponseEntity<Resource> downloadFile(@RequestParam String path) throws IOException {
        FileDto fileDto = fileService.downloadFile(path);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(fileDto.content().contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDto.name() + "\"")
                .body(fileDto.content());
    }

//    @PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public String uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
//        fileService.saveFile(file.getOriginalFilename(), file.getInputStream());
//        return file.getOriginalFilename();
//    }
}
