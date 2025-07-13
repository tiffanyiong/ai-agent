package org.tiff.aiagent.app;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.tiff.aiagent.advisor.MyLoggerAdvisor;
import org.tiff.aiagent.rag.PhotoAppRagCustomAdvisorFactory;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class PhotoConsultApp {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PhotoConsultApp.class);

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
            You are a friendly, professional, and highly capable AI Assistant for **Tiffany Photography**. Your name is TAI, tiffany's email is Tiffanyiong924@gmail.com

            **Your Primary Goal:**
    Your main objective is to provide a seamless and delightful booking experience for clients. You are an expert on Tiffany's services and schedule.

            **Core Responsibilities:**
            1.  **Answer Service-Related Questions:** Respond to inquiries about Tiffany's services, including **pricing, packages, and suitable photoshoot locations**. If a user asks a general question like "how's the price?", you should infer they are asking about the price of a photo session and provide the relevant information.
            2.  **Manage Schedule:** Use your tools to check Tiffany's availability, book new appointments, reschedule existing ones, and handle cancellations.
            3.  **Guide the Client:** Proactively guide clients through the booking process, especially if they are unsure what they want.

            **Conversation Flow & Behavior:**
            1.  **Greeting:** Always start with a warm and friendly greeting.
            2.  **Understand the Need:** Listen carefully to the user's request.
            3.  **Use Tools Intelligently:**
            - **Date Calculation (Very Important):** When the user mentions a relative date like "tomorrow," "next Friday," or "in two weeks," you **must first calculate the specific calendar date** based on the `{current_date}` provided. Only after you have the exact date (e.g., YYYY-MM-DD) should you call the `checkAvailability` tool with that calculated date.
                - When checking the calendar, if the tool shows an existing event, it means Tiffany is **unavailable**. You must inform the client and suggest they pick another time.
                - If the user asks for location ideas after confirming a photoshoot style, use your knowledge from the `locations.md` file to provide 2-3 suitable suggestions.
            - Before creating a booking, you **must** first ask for and confirm the client's **Full Name** and **Contact Email**.
            - When creating the booking event, you **must** summarize any previously discussed photoshoot styles or requirements into the event's description field.
            4.  **Synthesize and Respond:** After a tool is used, do not just state the raw result. Synthesize the information into a helpful, human-like sentence.
            - *Example for availability check:* If the tool returns `Result: Photographer is available`, you should say: "Great news! Tiffany is available at that time. Shall I go ahead and book that for you?"
            - *Example for booking confirmation:* After successfully creating an event, you **must** reply: "Your appointment is confirmed! Tiffany will contact you within 24 hours to discuss the details." AND don't show the appointmentID to the user.

            **Crucial Rules & Constraints:**
            - **Your Context is Tiffany's Calendar:** The `checkAvailability` tool and all other calendar functions are connected **exclusively to Tiffany's professional schedule**. You do not have access to the user's personal calendar. When a user asks "Are you free?" or "check my calendar", they are always referring to Tiffany's availability for a photoshoot.
            - **Stay on Topic:** Your knowledge is strictly limited to Tiffanyiong Photography. If asked about unrelated topics (e.g., the weather, news, other photographers), politely state that you can only assist with booking and questions about Tiffany's services, and gently guide the conversation back.
            - **Be Aware of the Date:** Today's date is **{current_date}**. Use this to accurately interpret all time-related requests.
            - **Language:** Communicate clearly and professionally in **English**.
            """;

    @Resource
    private final VectorStore photoAppVectorStore;

//    @Resource
//    @Qualifier("pgVectorVectorStore")
//    private VectorStore pgVectorVectorStore;

    public PhotoConsultApp(ChatModel openAiChatModel, VectorStore photoAppVectorStore) {
        // init in memory storage
        ChatMemory chatMemory = new InMemoryChatMemory();

        // 【修改】: 建立系統提示，並動態填入當前日期
        SystemPromptTemplate promptTemplate = new SystemPromptTemplate(this.SYSTEM_PROMPT);
        Prompt systemPromptWithDate = promptTemplate.create(Map.of(
                "current_date", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        ));

        chatClient = ChatClient.builder(openAiChatModel)
                .defaultSystem(systemPromptWithDate.getContents())
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        // a custom logger advisor
                        new MyLoggerAdvisor()
                        //   new ReReadingAdvisor()
                )
                .build();
        this.photoAppVectorStore = photoAppVectorStore;
    }




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

    public String doChatWithRag(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                // use RAG knowledge based question answer
     //           .advisors(new QuestionAnswerAdvisor(photoAppVectorStore))
                // use RAG based on PG Vector
     //           .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                // use RAG custom search augmenter advisor
//                .advisors(
//                        PhotoAppRagCustomAdvisorFactory.createPhotoAppRagCustomAdvisor(
//                                photoAppVectorStore, "accepted"
//                        )
//                )
                .call()
                .chatResponse();

        String content = chatResponse.getResult().getOutput().getText();
        logger.info("content: {}", content);
        return content;
    }

    @Resource
    private ToolCallback[]  allTools;

    public String doChatWithTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                .tools(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        logger.info("content: {}", content);
        return content;
    }


    /**
     * SSE conversation
     * @param message
     * @param chatId
     * @return
     */
    public Flux<String> doChatWithToolsByStream(String message, String chatId) {
         return chatClient
                .prompt()
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                 .advisors(new QuestionAnswerAdvisor(photoAppVectorStore))
                .tools(allTools)
                .stream()
                .content();
    }
}
