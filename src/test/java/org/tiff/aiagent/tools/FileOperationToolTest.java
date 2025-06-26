package org.tiff.aiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class FileOperationToolTest {

    @Test
    void testReadFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        String filename = "testing1.txt";
        String file = fileOperationTool.readFile(filename);
        Assertions.assertNotNull(file);
    }

    @Test
    void testWriteFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        String content = "Hello World!";
        String filename = "testing2.txt";
        String file = fileOperationTool.writeFile(filename, content);
        Assertions.assertNotNull(file);
    }
}