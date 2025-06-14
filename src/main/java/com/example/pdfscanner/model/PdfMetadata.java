package com.example.pdfscanner.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

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

    @Column(length = 500)
    private String producer;

    @Column()
    private String author;

    @Column(name = "created")
    private OffsetDateTime created;

    @Column(name = "modified")
    private OffsetDateTime modified;

    @Column(name = "scanned")
    private OffsetDateTime scanned;
}