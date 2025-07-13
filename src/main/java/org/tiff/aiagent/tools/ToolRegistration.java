package org.tiff.aiagent.tools;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tiff.aiagent.tools.calendar.CalendarTool;


@Configuration
public class ToolRegistration {
    private final CalendarTool calendarTool;

    public ToolRegistration(CalendarTool calendarTool) {
        this.calendarTool = calendarTool;
    }

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
                terminateTool,
                calendarTool
        );
    }
}
