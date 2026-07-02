package com.example.account.controller;

import com.example.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 账户对外接口
 */
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * 扣减余额接口
     * @param userId 用户ID
     * @param money 扣减金额
     * @return 操作结果
     */
    @PostMapping("/decrease")
    public String decrease(@RequestParam Long userId, @RequestParam BigDecimal money) {
        accountService.decrease(userId, money);
        return "账户余额扣减成功";
    }
}