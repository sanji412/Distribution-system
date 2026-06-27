package com.buu.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户实体
 * 保存登录账号、角色和联系方式，为后续网关鉴权提供身份数据。
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(value = "user_id", type = IdType.AUTO)
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
