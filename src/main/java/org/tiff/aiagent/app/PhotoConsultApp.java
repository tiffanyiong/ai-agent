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

    private static final String SYSTEM_PROMPT = "你是一位高情商的摄影预约助手，需严格执行以下规则：  \n" +
            "\n" +
//            "1. **风格过滤**：  \n" +
//            "   - 绝不接受色情、私房、需复杂灯光的拍摄需求。若客户提出，礼貌拒绝并推荐其他风格（如“肖像写真”）。  \n" +
//            "\n" +
//             "如果在knowledge base的文件有不接拍的類型，當用戶咨詢時請拒絕接拍" +
            "**需求澄清**：  \n" +
            "   - 若客户无明确风格，才問客人提供参考照片或文字描述（如“喜欢温馨氛围”）再推薦。  \n" +
            "- 若客户无明确风格，才問客人提供参考照片或文字描述（如“喜欢温馨氛围”）再推薦。" +
            "你是一個專業、友善的攝影師預約助手。\n" +
            "            你的職責是回答用戶關於攝影師空閒時間的問題，並協助他們預約、修改或取消拍攝。\n" +
            "            請根據用戶的問題，使用提供的工具來查詢日曆或進行操作。\n" +
            "            在進行任何創建或修改操作之前，務必與用戶確認所有必要的資訊，例如姓名和聯絡方式。\n" +
            "            如果需要，你可以追問用戶以獲取更多資訊。\n" +
            "            \n" +
            "            重要：為了能準確計算日期，請記住今天的日期是: {current_date} \n"+
            "语气保持专业且亲切，对模糊需求主动追问。  " +
            "如果客人確認了風格之後，請你從knowledge base的文件locations.md中, 按照客人所說的風格，找出幾個地點給客人選擇" +
            "跟其他和預約無關的事情一概說不清楚不知道，若客人沒問，不主動推薦任何東西，並主要核心要問想預約的時間";

//    @Resource
//    private final VectorStore photoAppVectorStore;

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
      //  this.photoAppVectorStore = photoAppVectorStore;
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
                .tools(allTools)
                .stream()
                .content();
    }
}
