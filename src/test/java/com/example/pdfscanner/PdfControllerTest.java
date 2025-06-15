package com.example.pdfscanner;

import ch.qos.logback.core.util.FileUtil;
import com.example.pdfscanner.controller.PdfController;
import com.example.pdfscanner.model.PdfMetadata;
import com.example.pdfscanner.service.PdfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.io.IOException;
import java.io.InputStream;
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

    @SpyBean
    private PdfController pdfController;

    private MockMultipartFile validPdfFile;
    private MockMultipartFile invalidFile;
    private PdfMetadata sampleMetadata;
    private final String sampleHash = "dGVzdGhhc2g="; // base64 encoded "testhash"

    @BeforeEach
    void setUp() throws IOException {

        byte[] pdfContent;

        try (InputStream is = FileUtil.class.getClassLoader().getResourceAsStream("sample1.pdf")) {
            if (is == null) throw new IOException("File not found");
            pdfContent = is.readAllBytes();
        }


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
    void testScanPdfSuccess() throws Exception {

        doReturn(sampleHash).when(pdfController).sha256(any(byte[].class));

        doNothing().when(pdfService).extractMetadata(any(byte[].class), anyString(), anyString());

        mockMvc.perform(multipart("/scan")
                        .file(validPdfFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sha256").value(sampleHash))
                .andDo(MockMvcResultHandlers.print());

        verify(pdfService, times(1)).extractMetadata(
                eq(validPdfFile.getBytes()),
                eq(sampleHash),
                contains("sample_")
        );
    }


    @Test
    void testScanPdfInvalidFileType() throws Exception {
        mockMvc.perform(multipart("/scan")
                        .file(invalidFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid file type"))
                .andDo(MockMvcResultHandlers.print());

        verify(pdfService, never()).extractMetadata(any(), any(), any());
    }

    @Test
    void testLookupMetadataSuccess() throws Exception {
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
    void testLookupMetadataNotFound() throws Exception {
        String nonExistentHash = "nonexistenthash";

        when(pdfService.lookup(nonExistentHash)).thenReturn(null);

        mockMvc.perform(get("/lookup/{hash}", nonExistentHash))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Metadata not found"))
                .andDo(MockMvcResultHandlers.print());

        verify(pdfService, times(1)).lookup(nonExistentHash);
    }
}