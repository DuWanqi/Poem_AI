package com.example.poemai.service;

import com.example.poemai.dto.AiCompletionApiRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class AiCompletionService {

    @Value("${app.deepseek.api-key}")
    private String apiKey;

    private final String apiUrl = "https://api.deepseek.com/v1/services/aigc/text-generation/generation";

    private final RestTemplate restTemplate;

    public AiCompletionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String completePoem(AiCompletionApiRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        // 构建请求体
        Map<String, Object> promptMap = Collections.singletonMap(
            "content",
            request.getPartialText() + "\n" + request.getUserPrompt()
        );

        Map<String, Object> requestBody = Map.of(
            "model", "deepseek-chat",
            "prompt", Collections.singletonList(promptMap),
            "max_tokens", 100,
            "temperature", 0.7
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // 修复点：使用exchange()代替postForEntity()[9,10](@ref)
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            apiUrl,
            HttpMethod.POST,  // 明确指定POST方法
            entity,
            new ParameterizedTypeReference<Map<String, Object>>() {}  // 保留泛型信息
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> body = response.getBody();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");

            if (choices != null && !choices.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

                if (message != null && message.containsKey("content")) {
                    return (String) message.get("content");
                }
            }
            throw new RuntimeException("AI 补全失败：响应中未找到 content 内容");
        } else {
            throw new RuntimeException("AI 补全失败，状态码：" + response.getStatusCode());
        }
    }
}