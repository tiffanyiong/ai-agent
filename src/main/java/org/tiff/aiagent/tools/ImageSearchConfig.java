package org.tiff.aiagent.tools;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

public class ImageSearchConfig {
    @Value("${pexels.api.key}")
    private String pexelsApiKey;

    @Bean
    public RestTemplate pexelsRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("Authorization", pexelsApiKey);
            return execution.execute(request, body);
        });

        // 返回已配置好的 RestTemplate 實例
        return restTemplate;
    }
}

