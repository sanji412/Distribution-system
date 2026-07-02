package com.example.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * 账户服务远程调用客户端
 */
@FeignClient(name = "account-service", url = "http://localhost:8083")
public interface AccountFeignClient {

    /**
     * 调用账户服务扣减余额
     * @param userId 用户ID
     * @param money 扣减金额
     * @return 操作结果
     */
    @PostMapping("/account/decrease")
    String decrease(@RequestParam("userId") Long userId,
                    @RequestParam("money") BigDecimal money);
}