package org.tiff.aiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MyMultiQueryExpanderTest {
    @Resource
    private MyMultiQueryExpander myMultiQueryExpander;

    @Test
    void testExpand() {
        List<Query> expand = myMultiQueryExpander.expand("HAHAAAaa i'm hungry where i can find food ah? wowwwww lalalabaa");
        Assertions.assertNotNull(expand);
    }
}