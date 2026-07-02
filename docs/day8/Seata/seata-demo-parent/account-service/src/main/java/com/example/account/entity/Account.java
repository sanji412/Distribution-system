package com.example.account.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 账户实体类
 * 对应数据库表 t_account
 */
@Data
@TableName("t_account")
public class Account {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 账户余额
     */
    private BigDecimal balance;
}