package org.tiff.aiagent.rag;

import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

/**
 * RAG 檢索增強advisor的工廠
 */
public class PhotoAppRagCustomAdvisorFactory {
    /**
     * Create a customer RAG search augmenter advisor
     * @param vectorStore vector store
     * @param style style
     * @return a customer RAG search augmenter advisor
     */
    public static Advisor createPhotoAppRagCustomAdvisor(VectorStore vectorStore, String style) {
        // filter out specific docs
        Filter.Expression filterExpression = new FilterExpressionBuilder()
                //不等於 過濾的條件，這樣AI不會讀到指定的文件
                //反之 用 eq, 那就指定了特定的文件而已
           //     .ne("style", style)
                .eq("style", style)
                .build();

        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .filterExpression(filterExpression) //filter condition
                .similarityThreshold(0.8)
                .topK(5) // return the number of documents
                .build();

        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                // 查詢不到的時候，指定AI回覆的內容
                .queryAugmenter(PhotoAppContextualQueryAugmenterFactory.createContextualQueryAugmenter())
                .build();

    }
}
