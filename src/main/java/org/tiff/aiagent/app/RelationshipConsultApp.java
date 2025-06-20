package org.tiff.aiagent.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import org.tiff.aiagent.advisor.MyLoggerAdvisor;
import org.tiff.aiagent.advisor.ReReadingAdvisor;
import org.tiff.aiagent.chatmemory.FileBasedChatMemory;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class RelationshipConsultApp {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RelationshipConsultApp.class);

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "You are a seasoned Love & Relationship Consultant " +
            "specializing in dating psychology, relationship dynamics, and marital conflict resolution. " +
            "Your approach blends empathy, active listening, and structured problem-solving to help users " +
            "navigate their unique challenges.";

    public RelationshipConsultApp(ChatModel openAiChatModel ) {
        //init in memory storage
        //ChatMemory chatMemory = new InMemoryChatMemory();

        // File Based chat memory
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        chatClient = ChatClient.builder(openAiChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        // a custom logger advisor
                        new MyLoggerAdvisor()
                     //   new ReReadingAdvisor()
                )
                .build();
    }

    /**
     * AI fundamental conversation
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();

        String content = chatResponse.getResult().getOutput().getText();
        logger.info("content: {}", content);
        return content;
    }

    record RelationshipReport(String title, List<String> suggestions){

    }

    /**
     * structuredOutput
     * @param message
     * @param chatId
     * @return
     */
    public RelationshipReport doChatWithReport(String message, String chatId) {
        RelationshipReport report = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "generate a report in every coversation, " +
                        "title is {user's name} report, the report content is a list of suggestion. if the user doesn't" +
                        "provide the name, then the report title is 'your report' ")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(RelationshipReport.class);

        logger.info("report: {}", report);
        return report;
    }
}
