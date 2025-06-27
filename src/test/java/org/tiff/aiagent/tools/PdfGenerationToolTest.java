package org.tiff.aiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PdfGenerationToolTest {

    @Test
    void generatePDF() {
        PdfGenerationTool pdfGenerationTool = new PdfGenerationTool();
        String content = "Hello World!";
        String filename = "pdf-generation-testing.pdf";
        String result = pdfGenerationTool.generatePDF(filename, content);
        Assertions.assertNotNull(result);
    }
}