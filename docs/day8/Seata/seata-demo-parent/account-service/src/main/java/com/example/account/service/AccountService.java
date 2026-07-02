package com.example.account.service;

import com.example.account.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 账户业务逻辑层
 * RM角色，Seata自动管理分支事务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountMapper accountMapper;

    /**
     * 扣减用户账户余额
     * @param userId 用户ID
     * @param money 扣减金额
     */
    public void decrease(Long userId, BigDecimal money) {
        log.info("【账户服务】开始扣减余额，用户ID：{}，扣减金额：{}", userId, money);

        int rows = accountMapper.decreaseBalance(userId, money);
        if (rows == 0) {
            throw new RuntimeException("账户余额不足，扣减失败");
        }

        log.info("【账户服务】余额扣减成功");
    }
}