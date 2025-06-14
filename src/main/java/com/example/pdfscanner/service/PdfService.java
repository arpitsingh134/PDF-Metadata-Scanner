package com.example.pdfscanner.service;

import com.example.pdfscanner.model.PdfMetadata;
import com.example.pdfscanner.repository.PdfMetadataRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
public class PdfService {

    @Autowired
    private PdfMetadataRepository repository;

    @Async
    public void extractMetadata(byte[] content, String sha256, String filename) throws IOException {
        log.info("Starting metadata extraction for file: {}", filename);

        try (PDDocument document = PDDocument.load(content)) {
            PDDocumentInformation info = document.getDocumentInformation();

            PdfMetadata metadata = PdfMetadata.builder()
                    .sha256(sha256)
                    .version(String.valueOf(document.getVersion()))
                    .producer(info.getProducer())
                    .author(info.getAuthor())
                    .created(String.valueOf(info.getCreationDate()))
                    .modified(String.valueOf(info.getModificationDate()))
                    .scanned(OffsetDateTime.now(ZoneOffset.UTC).toString())
                    .filename(filename)
                    .build();

            repository.save(metadata);
            log.info("Metadata saved successfully for hash: {}", sha256);
        } catch (IOException e) {
            log.error("Error extracting metadata for file: {}", filename, e);
            throw e;
        }
    }

    public PdfMetadata lookup(String hash) {
        log.info("Looking up metadata for hash: {}", hash);

        PdfMetadata metadata = repository.findById(hash).orElse(null);
        if (metadata != null) {
            log.info("Metadata found for hash: {}", hash);
        } else {
            log.warn("No metadata found for hash: {}", hash);
        }
        return metadata;
    }
}
