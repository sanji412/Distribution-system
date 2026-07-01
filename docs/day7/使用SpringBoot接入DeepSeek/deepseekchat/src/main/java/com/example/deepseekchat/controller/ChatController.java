package com.example.deepseekchat.controller;

import com.example.deepseekchat.service.DeepSeekService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 聊天接口控制器
 * 作用：提供前端页面调用的HTTP接口，接收用户问题，返回AI回复
 */
@RestController // 标识这是接口控制器，返回JSON格式数据
@RequestMapping("/api/chat") // 接口统一前缀
@RequiredArgsConstructor
@CrossOrigin // 允许跨域：前端页面和后端端口不同时也能访问
public class ChatController {

    // 注入DeepSeek服务
    private final DeepSeekService deepSeekService;

    /**
     * 聊天接口：接收用户消息，返回AI回复
     * @param requestMap 请求体，包含message字段（用户输入的内容）
     * @return 包含AI回复的JSON结果
     */
    @PostMapping
    public Map<String, Object> chat(@RequestBody Map<String, String> requestMap) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 从请求中获取用户输入的消息
            String userMessage = requestMap.get("message");

            // 2. 参数校验：消息不能为空
            if (userMessage == null || userMessage.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "消息内容不能为空");
                return result;
            }

            // 3. 调用服务，获取AI回复
            String reply = deepSeekService.chat(userMessage.trim());
//            String reply = deepSeekService.customerServiceChat(userMessage.trim());

            // 4. 封装成功结果返回
            result.put("success", true);
            result.put("reply", reply);

        } catch (Exception e) {
            // 异常处理：打印错误日志，返回失败信息
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "调用AI失败：" + e.getMessage());
        }

        return result;
    }
}