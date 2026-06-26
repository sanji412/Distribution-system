package com.buu.distribution.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long userId;
    private String username;
    private String password;
    private String nickname;
    private String role;
    private Integer status;
    private String phone;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
