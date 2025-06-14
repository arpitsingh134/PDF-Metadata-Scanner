package com.example.pdfscanner.controller;

import com.example.pdfscanner.model.PdfMetadata;
import com.example.pdfscanner.service.PdfService;
import com.example.pdfscanner.util.HashUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@RestController
public class PdfController {

    @Autowired
    private PdfService pdfService;

    @PostMapping("/scan")
    public ResponseEntity<Map<String, String>> scan(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid file type"));
        }

        String sha256 = HashUtil.sha256(file.getBytes());

        // Append timestamp to filename
        String originalName = file.getOriginalFilename().replaceAll("\\.pdf$", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        String newFileName = originalName + "_" + timestamp + ".pdf";

        pdfService.extractMetadata(file.getBytes(), sha256, newFileName);
        return ResponseEntity.ok(Map.of("sha256", sha256));
    }

    @GetMapping("/lookup/{hash}")
    public ResponseEntity<?> lookup(@PathVariable String hash) {
        try {
            String decodedHash = URLDecoder.decode(hash, StandardCharsets.UTF_8.toString());
            log.info("Lookup request received for hash: {}", decodedHash);

            PdfMetadata metadata = pdfService.lookup(decodedHash);

            if (metadata == null) {
                log.warn("No metadata found for hash: {}", decodedHash);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Metadata not found"));
            }

            log.info("Metadata found for hash: {}", decodedHash);
            return ResponseEntity.ok(metadata);
        } catch (Exception e) {
            log.error("Invalid hash format for lookup: {}", hash, e);
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid hash format"));
        }
    }
}
