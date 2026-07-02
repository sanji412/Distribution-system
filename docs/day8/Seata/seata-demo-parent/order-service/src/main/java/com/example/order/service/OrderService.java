package com.example.order.service;

import com.example.order.entity.Order;
import com.example.order.feign.AccountFeignClient;
import com.example.order.feign.StorageFeignClient;
import com.example.order.mapper.OrderMapper;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final StorageFeignClient storageFeignClient;
    private final AccountFeignClient accountFeignClient;

    /**
     * 创建订单（全局分布式事务入口）
     * @GlobalTransactional 管控跨服务的全局分布式事务
     * @Transactional 管控当前服务内的本地数据库事务，将多次DB操作合并为一个分支事务
     */
    @GlobalTransactional(name = "create-order-tx", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(Long userId, Long productId, Integer count, BigDecimal money) {
        log.info("【订单服务】全局事务启动，XID：{}", RootContext.getXID());

        // 1. 本地创建订单（初始状态0）
        log.info("【订单服务】开始创建订单");
        Order order = new Order();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setCount(count);
        order.setMoney(money);
        order.setStatus(0);
        orderMapper.insert(order);
        log.info("【订单服务】订单创建成功，订单ID：{}", order.getId());

        // 2. 远程调用：扣减商品库存
        log.info("【订单服务】调用库存服务扣减库存");
        storageFeignClient.decrease(productId, count);

        // 3. 远程调用：扣减用户账户余额
        log.info("【订单服务】调用账户服务扣减余额");
        accountFeignClient.decrease(userId, money);

        // 4. 本地更新订单状态为已完成
        log.info("【订单服务】更新订单状态为已完成");
        order.setStatus(1);
        orderMapper.updateById(order);

        // ========== 测试回滚：放开注释模拟异常 ==========
//        int i = 1 / 0;

        log.info("【订单服务】全局事务执行完成，XID：{}", RootContext.getXID());
    }
}