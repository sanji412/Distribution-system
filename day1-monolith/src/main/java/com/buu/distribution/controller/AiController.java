package com.buu.distribution.controller;

import com.buu.distribution.common.ApiResponse;
import com.buu.distribution.dto.AiChatRequest;
import com.buu.distribution.dto.AiChatResponse;
import com.buu.distribution.entity.AiChatRecord;
import com.buu.distribution.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    public ApiResponse<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        return ApiResponse.success(aiService.chat(request));
    }

    @GetMapping("/history")
    public ApiResponse<List<AiChatRecord>> history(@RequestParam(required = false) Long userId) {
        return ApiResponse.success(aiService.history(userId));
    }

    @GetMapping("/recommendations")
    public ApiResponse<List<String>> recommendations() {
        return ApiResponse.success(aiService.recommendations());
    }
}
