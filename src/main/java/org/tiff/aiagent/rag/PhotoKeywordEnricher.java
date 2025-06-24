package org.tiff.aiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Ues AI model to add keyword to each chunk's metadata
 */
@Component
public class PhotoKeywordEnricher {

    @Resource
    private final ChatModel openAiChatModel;

    public PhotoKeywordEnricher(ChatModel openAiChatModel) {
        this.openAiChatModel = openAiChatModel;
    }

    List<Document> enrichDocuments(List<Document> documents) {
        KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(this.openAiChatModel, 5);
        return enricher.apply(documents);
    }

}
