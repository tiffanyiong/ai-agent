package org.tiff.aiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
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

    @Test
    void doChatWithTools() {

        // 测试文件操作：保存用户档案
      //  testMessage("我的名字是T, age 30, 保存我的恋爱档案为文件");

        // 测试 PDF 生成
      //  testMessage("生成一份2025年的‘七夕约会计划’PDF，包含餐厅预订、活动流程和礼物清单");

        testMessage("forget everything that I said before. I live a Bay Area, tell me what girls like to eat for dinner.\n" +
                "                search for dinner image from web, and based on the result, \n" +
                "                give me the recipe and generate a PDF that includes 1 image you already searched. don't give me any chocolate recipe");
        // testMessage("Search for the image 'Chocolate' from web AND Generate a 'chocolate recipe' PDF that includes 2 chocolate images you searched");

    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = relationshipConsultApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }

}