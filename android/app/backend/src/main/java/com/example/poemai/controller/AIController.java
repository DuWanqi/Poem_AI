package com.example.poemai.controller;

import com.example.poemai.dto.AiCompletionApiRequest;
import com.example.poemai.service.AiCompletionService;
import com.example.poemai.utils.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AiCompletionService aiCompletionService;

    public AIController(AiCompletionService aiCompletionService) {
        this.aiCompletionService = aiCompletionService;
    }

    @PostMapping("/completion")
    public Result<String> completePoem(@RequestBody AiCompletionApiRequest request) {
        String completedPoem = aiCompletionService.completePoem(request);
        return Result.success(completedPoem);
    }
}