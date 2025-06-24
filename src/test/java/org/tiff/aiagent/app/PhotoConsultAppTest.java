package org.tiff.aiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PhotoConsultAppTest {
    @Resource
    PhotoConsultApp photoConsultApp;

    @Test
    void testDoChat() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好呀，我想咨詢約拍，請問怎麼收費？";
        String response = photoConsultApp.doChat(message, chatId);
        Assertions.assertNotNull(response);

        message = "你給我推薦好吃的餐廳？ ";
        response = photoConsultApp.doChat(message, chatId);
        Assertions.assertNotNull(response);
    }

    @Test
    void testDoChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好呀，我想咨詢約拍，請問怎麼收費？";
        String response = photoConsultApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(response);

        message = "我想要拍日系的個人寫真";
        response = photoConsultApp.doChat(message, chatId);
        Assertions.assertNotNull(response);

        message = "我是短髮長得比較可愛";
        response = photoConsultApp.doChat(message, chatId);
        Assertions.assertNotNull(response);


        message = "想要拍日系小清新的照片";
        response = photoConsultApp.doChat(message, chatId);
        Assertions.assertNotNull(response);


        message = "但是我懷孕了，想拍的是日系小清新的孕婦照，和先生一起拍";
        response = photoConsultApp.doChat(message, chatId);
        Assertions.assertNotNull(response);

        message = "那收費是多少嗎？ 我想在下周的 12點拍照，有甚麼地方推薦嗎？";
        response = photoConsultApp.doChat(message, chatId);
        Assertions.assertNotNull(response);

        message = "6月20日的 12點拍照，有甚麼地方推薦嗎？";
        response = photoConsultApp.doChat(message, chatId);
        Assertions.assertNotNull(response);

        message = "公園吧，你推薦在san jose哪個公園啊";
        response = photoConsultApp.doChat(message, chatId);
        Assertions.assertNotNull(response);

    }

    @Test
    void testRAE_PgVector(){
        String chatId = UUID.randomUUID().toString();
        String message = "你好呀，我想咨詢約拍，請問怎麼收費？";
        String response = photoConsultApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(response);

    }
}