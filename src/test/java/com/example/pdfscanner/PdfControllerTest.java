package com.example.pdfscanner;

import com.example.pdfscanner.controller.PdfController;
import com.example.pdfscanner.model.PdfMetadata;
import com.example.pdfscanner.service.PdfService;
import com.example.pdfscanner.util.HashUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PdfController.class)
class PdfControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PdfService pdfService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMultipartFile validPdfFile;
    private MockMultipartFile invalidFile;
    private MockMultipartFile emptyFile;
    private PdfMetadata sampleMetadata;
    private final String sampleHash = "dGVzdGhhc2g="; // base64 encoded "testhash"

    @BeforeEach
    void setUp() {
        // Create sample PDF file content (minimal PDF structure)
        byte[] pdfContent = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n%%EOF".getBytes();

        validPdfFile = new MockMultipartFile(
                "file",
                "sample.pdf",
                "application/pdf",
                pdfContent
        );

        invalidFile = new MockMultipartFile(
                "file",
                "sample.txt",
                "text/plain",
                "This is not a PDF file".getBytes()
        );

        emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        // Create sample metadata
        sampleMetadata = PdfMetadata.builder()
                .sha256(sampleHash)
                .filename("sample_123456789.pdf")
                .version("1.4")
                .producer("TestProducer")
                .author("TestAuthor")
                .created(OffsetDateTime.parse("2024-01-01T10:00:00Z"))
                .modified(OffsetDateTime.parse("2024-01-01T10:00:00Z"))
                .scanned(OffsetDateTime.parse(OffsetDateTime.now(ZoneOffset.UTC).toString()))
                .build();
    }

    @Test
    void testScanPdf_Success() throws Exception {
        // Mock HashUtil.sha256 to return a predictable hash
        try (MockedStatic<HashUtil> hashUtilMock = Mockito.mockStatic(HashUtil.class)) {
            hashUtilMock.when(() -> HashUtil.sha256(any(byte[].class)))
                    .thenReturn(sampleHash);

            // Mock service method (void method, so just verify it's called)
            doNothing().when(pdfService).extractMetadata(any(byte[].class), anyString(), anyString());

            mockMvc.perform(multipart("/scan")
                            .file(validPdfFile))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.sha256").value(sampleHash))
                    .andDo(MockMvcResultHandlers.print());

            // Verify service method was called with correct parameters
            verify(pdfService, times(1)).extractMetadata(
                    eq(validPdfFile.getBytes()),
                    eq(sampleHash),
                    contains("sample_") // filename should contain original name + timestamp
            );
        }
    }


    @Test
    void testScanPdf_InvalidFileType() throws Exception {
        mockMvc.perform(multipart("/scan")
                        .file(invalidFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid file type"))
                .andDo(MockMvcResultHandlers.print());

        // Verify service method was never called
        verify(pdfService, never()).extractMetadata(any(), any(), any());
    }

    @Test
    void testLookupMetadata_Success() throws Exception {
        // Mock service to return sample metadata
        when(pdfService.lookup(sampleHash)).thenReturn(sampleMetadata);

        mockMvc.perform(get("/lookup/{hash}", sampleHash))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sha256").value(sampleHash))
                .andExpect(jsonPath("$.filename").value("sample_123456789.pdf"))
                .andExpect(jsonPath("$.version").value("1.4"))
                .andExpect(jsonPath("$.producer").value("TestProducer"))
                .andExpect(jsonPath("$.author").value("TestAuthor"))
                .andExpect(jsonPath("$.created").value("2024-01-01T10:00:00Z"))
                .andExpect(jsonPath("$.modified").value("2024-01-01T10:00:00Z"))
                .andExpect(jsonPath("$.scanned").exists())
                .andDo(MockMvcResultHandlers.print());

        verify(pdfService, times(1)).lookup(sampleHash);
    }

    @Test
    void testLookupMetadata_NotFound() throws Exception {
        String nonExistentHash = "nonexistenthash";

        // Mock service to return null (not found)
        when(pdfService.lookup(nonExistentHash)).thenReturn(null);

        mockMvc.perform(get("/lookup/{hash}", nonExistentHash))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Metadata not found"))
                .andDo(MockMvcResultHandlers.print());

        verify(pdfService, times(1)).lookup(nonExistentHash);
    }
}