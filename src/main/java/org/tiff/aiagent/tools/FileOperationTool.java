package org.tiff.aiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.tiff.aiagent.constant.FileConstant;

import java.io.File;

/**
 * File operation tools for AI
 */
public class FileOperationTool {

    private final String FILE_DIR = FileConstant.FILE_SAVE_DIR + File.separator + "file";

    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = "Name of a file to read") String fileName) {
        String filePath = FILE_DIR + File.separator + fileName;
        try {
            return FileUtil.readUtf8String(filePath);
        } catch (IORuntimeException e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    @Tool(description = "Write content to a file")
    public String writeFile(@ToolParam(description = "Name of a file to write") String fileName,
                            @ToolParam(description = "Content to write to the file") String content) {
        String filePath = FILE_DIR + File.separator + fileName;

        try {
            //create a direct
//            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content, filePath);
            return "File successfully written to: " + filePath;
        } catch (IORuntimeException e) {
            return "Error writing file: " + e.getMessage();
        }
    }
}
