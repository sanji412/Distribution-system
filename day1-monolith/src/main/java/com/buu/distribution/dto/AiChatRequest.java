package com.buu.distribution.dto;

import lombok.Data;

@Data
public class AiChatRequest {

    private Long userId;
    private String question;
}
