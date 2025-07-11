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
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tiff.aiagent.advisor.MyLoggerAdvisor;
import org.tiff.aiagent.rag.PhotoAppRagCustomAdvisorFactory;

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
            "- 若客户无明确风格，才問客人提供参考照片或文字描述（如“喜欢温馨氛围”）再推薦。  \n" +
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
        chatClient = ChatClient.builder(openAiChatModel)
                .defaultSystem(SYSTEM_PROMPT)
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
}
