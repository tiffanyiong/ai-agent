package org.tiff.aiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PhotoAppDocLoaderTest {
    @Resource
    private PhotoAppDocLoader photoAppDocLoader;

    @Test
    void testLoadMarkdowns() {
        photoAppDocLoader.loadMarkdowns();
    }
}