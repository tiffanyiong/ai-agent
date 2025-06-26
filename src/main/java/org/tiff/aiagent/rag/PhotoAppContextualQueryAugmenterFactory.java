package org.tiff.aiagent.rag;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

/**
 * create a contextual query augmenter factory
 */
public class PhotoAppContextualQueryAugmenterFactory   {
    public static ContextualQueryAugmenter createContextualQueryAugmenter() {
        PromptTemplate emptyPromptTemplate = new PromptTemplate(
                """
                   you should respond with the following content:
                   I'm sorry, I can only answer some photo session appointment questions.
                   If there's any other questions, please add photographer's wechat: tiffanyiong924"""
        );

        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(false)
                .emptyContextPromptTemplate(emptyPromptTemplate)
                .build();
    }
}
