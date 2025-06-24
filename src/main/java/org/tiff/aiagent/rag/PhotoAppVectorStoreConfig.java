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

    @Resource
    private PhotoKeywordEnricher photoKeywordEnricher;

    @Bean
    VectorStore photoAppVectorStore(EmbeddingModel openAiEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(openAiEmbeddingModel)
                .build();
        // load document
        List<Document> documents = photoAppDocLoader.loadMarkdowns();

        // use keyword enricher to add mroe metadata to the doc
        List<Document> enrichedDocs = photoKeywordEnricher.enrichDocuments(documents);
       //  simpleVectorStore.add(documents);
        simpleVectorStore.add(enrichedDocs);
        return simpleVectorStore;
    }
}
