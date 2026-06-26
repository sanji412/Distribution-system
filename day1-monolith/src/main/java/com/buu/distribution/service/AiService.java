package com.buu.distribution.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buu.distribution.dto.AiChatRequest;
import com.buu.distribution.dto.AiChatResponse;
import com.buu.distribution.entity.AiChatRecord;
import com.buu.distribution.entity.OrderMain;
import com.buu.distribution.entity.Product;
import com.buu.distribution.mapper.AiChatRecordMapper;
import com.buu.distribution.mapper.OrderMainMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AiService {

    private static final Pattern ORDER_NO_PATTERN = Pattern.compile("DD\\d{8}[A-Z0-9]*");

    private final AiChatRecordMapper aiChatRecordMapper;
    private final OrderMainMapper orderMainMapper;
    private final StockService stockService;

    public AiChatResponse chat(AiChatRequest request) {
        String answer = buildAnswer(request.getQuestion());
        AiChatRecord record = new AiChatRecord();
        record.setUserId(request.getUserId());
        record.setQuestion(request.getQuestion());
        record.setAnswer(answer);
        record.setCreateTime(LocalDateTime.now());
        aiChatRecordMapper.insert(record);
        return new AiChatResponse(answer);
    }

    public List<AiChatRecord> history(Long userId) {
        return aiChatRecordMapper.selectList(new LambdaQueryWrapper<AiChatRecord>()
                .eq(userId != null, AiChatRecord::getUserId, userId)
                .orderByDesc(AiChatRecord::getCreateTime));
    }

    public List<String> recommendations() {
        List<Product> warningProducts = stockService.listWarningProducts();
        String warning = warningProducts.isEmpty()
                ? "库存预警：当前暂无低于安全库存的商品。"
                : "库存预警：" + warningProducts.get(0).getProductName() + " 库存偏低，建议及时补货。";
        return List.of(
                "搭配推荐：浏览机械键盘的用户可搭配无线鼠标、笔记本支架和扩展坞。",
                warning,
                "热销趋势：近 7 天建议重点关注电脑外设和办公耗材。"
        );
    }

    private String buildAnswer(String question) {
        if (question == null || question.isBlank()) {
            return "您好，请输入需要咨询的问题。";
        }
        Matcher matcher = ORDER_NO_PATTERN.matcher(question);
        if (matcher.find()) {
            String orderNo = matcher.group();
            OrderMain order = orderMainMapper.selectOne(new LambdaQueryWrapper<OrderMain>().eq(OrderMain::getOrderNo, orderNo));
            if (order == null) {
                return "您好，未查询到订单 " + orderNo + "，请确认订单号是否正确。";
            }
            return "您好，您的订单 " + order.getOrderNo()
                    + " 当前状态为 " + order.getOrderStatus()
                    + "，商品为 " + order.getProductName()
                    + "，物流状态为 " + emptyToDefault(order.getLogisticsStatus(), "暂无物流信息")
                    + "，当前位置为 " + emptyToDefault(order.getCurrentLocation(), "暂无更新") + "。";
        }
        if (question.contains("库存")) {
            return recommendations().get(1);
        }
        if (question.contains("推荐") || question.contains("搭配")) {
            return recommendations().get(0);
        }
        if (question.contains("热销") || question.contains("趋势")) {
            return recommendations().get(2);
        }
        return "您好，我可以帮您查询订单物流、库存预警、商品搭配推荐和热销趋势。";
    }

    private String emptyToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
