package org.tiff.aiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SpringAiInvoke implements CommandLineRunner {

    @Resource
    private ChatModel openAiChatModel;


    @Override
    public void run(String... args) throws Exception {
//        AssistantMessage  assistantMessage = openAiChatModel.call(new Prompt("Hello World"))
//                .getResult()
//                .getOutput();
//
//        System.out.println(assistantMessage.getText());
    }
}
