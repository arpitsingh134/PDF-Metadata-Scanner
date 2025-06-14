package com.example.pdfscanner.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pdf_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdfMetadata {
    @Id
    private String sha256;
    @Column(name = "filename")
    private String filename;
    private String version;
    private String producer;
    private String author;
    private String created;
    private String modified;
    private String scanned;
}
