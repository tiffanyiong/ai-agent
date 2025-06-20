package org.tiff.aiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RelationshipConsultAppTest {

    @Resource
    private RelationshipConsultApp relationshipConsultApp;
    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        // round 1
        String message = "Hi, my name is Tiff";
        String response = relationshipConsultApp.doChat(message, chatId);

        // round 2
        message = "My boyfriend is Alex, I want him to propose to me";
        response = relationshipConsultApp.doChat(message, chatId);
        Assertions.assertNotNull(response);

        // round 3
        message = "Do you remember what my boyfriend name is?";
        response = relationshipConsultApp.doChat(message, chatId);
        Assertions.assertNotNull(response);
    }


    @Test
    void testDoChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        // round 1
        String message = "Hi, my name is Tiff";
        String response = relationshipConsultApp.doChat(message, chatId);

        // round 2
        message = "My boyfriend is Alex, I want him to propose to me";
        RelationshipConsultApp.RelationshipReport report = relationshipConsultApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(report);


    }
}