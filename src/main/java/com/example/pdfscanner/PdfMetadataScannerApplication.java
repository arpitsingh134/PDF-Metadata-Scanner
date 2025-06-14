package com.example.pdfscanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PdfMetadataScannerApplication {
    public static void main(String[] args) {
        SpringApplication.run(PdfMetadataScannerApplication.class, args);
    }
}
