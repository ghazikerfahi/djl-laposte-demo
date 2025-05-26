package com.laposte.djl.controller;

import com.laposte.djl.service.ImageRecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/image")
public class ImageRecognitionController {

    @Autowired
    private ImageRecognitionService service;

    @PostMapping("/classify")
    public ResponseEntity<String> classify(@RequestParam("file") MultipartFile file) throws Exception {
        String result = service.predict(file);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/customClassify")
    public ResponseEntity<String> customClassify(@RequestParam("file") MultipartFile file) throws Exception {
        String result = service.predictCustom(file);
        return ResponseEntity.ok(result);
    }
}