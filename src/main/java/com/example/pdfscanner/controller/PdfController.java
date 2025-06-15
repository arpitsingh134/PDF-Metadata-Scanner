package com.example.pdfscanner.controller;

import com.example.pdfscanner.model.PdfMetadata;
import com.example.pdfscanner.service.PdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

@Slf4j
@RestController
public class PdfController {

    @Autowired
    private PdfService pdfService;

    @PostMapping("/scan")
    public ResponseEntity<Map<String, String>> scanPdfFile(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid file type"));
        }

        String sha256 = sha256(file.getBytes());

        String originalName = file.getOriginalFilename().replaceAll("\\.pdf$", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        String newFileName = originalName + "_" + timestamp + ".pdf";

        pdfService.extractMetadata(file.getBytes(), sha256, newFileName);
        return ResponseEntity.ok(Map.of("sha256", sha256));
    }

    @GetMapping("/lookup/{fileId}")
    public ResponseEntity<?> lookupMetaData(@PathVariable String fileId) {
        try {
            String decodeHash = URLDecoder.decode(fileId, StandardCharsets.UTF_8.toString());
            log.info("Lookup request received for hash: {}", decodeHash);

            PdfMetadata metadata = pdfService.lookup(decodeHash);

            if (metadata == null) {
                log.warn("No metadata found for hash: {}", decodeHash);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Metadata not found"));
            }

            log.info("Metadata found for hash: {}", decodeHash);
            return ResponseEntity.ok(metadata);
        } catch (Exception e) {
            log.error("Invalid hash format for lookup: {}", fileId, e);
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid hash format"));
        }
    }

    public String sha256(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        return Base64.getEncoder().encodeToString(hash);
    }

}
