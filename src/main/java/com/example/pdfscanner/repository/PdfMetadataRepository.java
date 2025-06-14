
package com.example.pdfscanner.repository;

import com.example.pdfscanner.model.PdfMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PdfMetadataRepository extends JpaRepository<PdfMetadata, String> {
}
