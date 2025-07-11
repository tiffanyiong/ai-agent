package org.tiff.aiagent.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.tiff.aiagent.agent.MockManus;
import org.tiff.aiagent.app.PhotoConsultApp;
import org.tiff.aiagent.app.RelationshipConsultApp;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController()
@RequestMapping("/ai")
public class AiController {

    @Resource
    private RelationshipConsultApp relationshipConsultApp;

    @Resource
    PhotoConsultApp photoConsultApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel openAiChatModel;


    /**
     * Sync the relationship consult app
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/relationship/chat/sync")
    public String doChatWithRelationshipSync(String message, String chatId){
        return relationshipConsultApp.doChat(message, chatId);
    }

    /**
     * SSE - Relationship consult app
     * 文本流式返回
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/relationship/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithRelationshipSSE(String message, String chatId){
        return relationshipConsultApp.doChatByStream(message, chatId);
    }

    /**
     * SSE - Relationship consult app
     * server sent event
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/relationship/chat/server_sent_event")
    public Flux<ServerSentEvent> doChatWithRelationshipSeverSentEvent(String message, String chatId){
        return relationshipConsultApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    /**
     * SSE - Relationship consult app
     * Sse Emitter - control which part that is sent to frontend
     * @param message
     * @param chatId
     * @return sse emitter
     */
    @GetMapping(value = "/relationship/chat/sse_emitter")
    public SseEmitter doChatWithRelationshipSseEmitter(String message, String chatId){
        // create a 3 min SseEmitter
        SseEmitter emitter = new SseEmitter(180000L); // 3 mins timeout
        // get a Flux stream data and subscribe and push message to SseEmitter
        relationshipConsultApp.doChatByStream(message, chatId)
                .subscribe(chunk -> {
                    try {
                        emitter.send(chunk);
                    } catch (IOException e) {
                      emitter.completeWithError(e);
                    }
                }, emitter::completeWithError, emitter::complete);
        return emitter;
    }

    /**
     * SSE - mock manus
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message){
       MockManus mockManus = new MockManus(allTools, openAiChatModel);
       return mockManus.runStream(message);
    }

    /**
     * Photo Consult App - chat
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/photo_consult/chat", produces =  MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithPhotoApp(String message, String chatId){
        return photoConsultApp.doChatWithToolsByStream(message, chatId);
    }


}
