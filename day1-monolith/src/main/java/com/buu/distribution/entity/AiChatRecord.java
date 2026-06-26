package com.buu.distribution.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_chat_record")
public class AiChatRecord {

    @TableId(type = IdType.AUTO)
    private Long chatId;
    private Long userId;
    private String question;
    private String answer;
    private LocalDateTime createTime;
}
