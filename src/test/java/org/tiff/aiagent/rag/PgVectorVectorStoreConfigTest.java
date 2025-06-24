package org.tiff.aiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PgVectorVectorStoreConfigTest {

    @Resource
    @Qualifier("pgVectorVectorStore")
    private VectorStore pgVectoreVectorStore;

    @Test
    void testPgVectorStoreConfig() {
        List<Document> documents = List.of(
            new Document("Shinn is a place where is good for taking japanese style photos", Map.of("meta1", "meta1")),
            new Document("Spring AI rocks!!"),
            new Document("Bay area has a lot of japanese style places", Map.of("meta2", "meta2")));

        // add doc
        pgVectoreVectorStore.add(documents);

        // checking
        List<Document> results = pgVectoreVectorStore.similaritySearch(SearchRequest.builder().query("japanese").topK(3).build());
        Assertions.assertNotNull(results);

    }
}

