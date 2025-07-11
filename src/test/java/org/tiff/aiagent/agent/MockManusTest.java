package org.tiff.aiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MockManusTest {
    @Resource
    private MockManus manus;

    @Test
    void run() {
        String userPrompt = """
                I live a Bay Area, tell me what girls like to eat for dinner.
                search for dinner image from web, and based on the result, 
                give me the recipe and generate a PDF that includes 1 image you already searched
                """;
        String answer = manus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }
}