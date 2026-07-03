package com.buu.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("auth_account")
public class AuthAccount {

    @TableId(value = "account_id", type = IdType.AUTO)
    private Long accountId;

    private String username;
    private String password;
    private String nickname;
    private String role;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
