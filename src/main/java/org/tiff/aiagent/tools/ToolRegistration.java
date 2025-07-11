package org.tiff.aiagent.tools;

import jakarta.annotation.Resource;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


@Configuration
public class ToolRegistration {


    @Bean
    public ToolCallback[] allTools() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        PdfGenerationTool pdfGenerationTool = new PdfGenerationTool();
        ImageSearchTool imageSearchTool = new ImageSearchTool();
        TerminateTool terminateTool = new TerminateTool();
        return ToolCallbacks.from(
                fileOperationTool,
                pdfGenerationTool,
                imageSearchTool,
                terminateTool
        );
    }
}
