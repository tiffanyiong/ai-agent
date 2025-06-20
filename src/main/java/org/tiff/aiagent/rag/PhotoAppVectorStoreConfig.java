package org.tiff.aiagent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.List;

@Configuration
@Slf4j
public class PhotoAppVectorStoreConfig {

    @Resource
    private PhotoAppDocLoader photoAppDocLoader;

    @Bean
    VectorStore photoAppVectorStore(EmbeddingModel openAiEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(openAiEmbeddingModel)
                .build();
        // load document
        List<Document> documents = photoAppDocLoader.loadMarkdowns();
        simpleVectorStore.add(documents);
        return simpleVectorStore;
    }
}
