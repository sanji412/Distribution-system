package com.buu.order.service.impl;

import com.buu.order.client.ProductFeignClient;
import com.buu.order.client.StockFeignClient;
import com.buu.order.common.R;
import com.buu.order.dto.AiChatRequest;
import com.buu.order.dto.AiChatResponse;
import com.buu.order.dto.AiIntentResult;
import com.buu.order.dto.RemoteProductDTO;
import com.buu.order.dto.RemoteStockDTO;
import com.buu.order.entity.AiChatRecord;
import com.buu.order.entity.OrderItem;
import com.buu.order.entity.OrderMain;
import com.buu.order.mapper.AiChatRecordMapper;
import com.buu.order.service.AiCustomerService;
import com.buu.order.service.AiModelClient;
import com.buu.order.service.AiPromptTemplateService;
import com.buu.order.service.OrderRemoteLocalQuery;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 客服业务实现
 * 先识别意图和槽位，再调用订单、商品、库存真实业务数据生成动态 Prompt。
 */
@Service
public class AiCustomerServiceImpl implements AiCustomerService {

    private static final Pattern ORDER_NO_PATTERN = Pattern.compile("DD\\d{8,}");
    private static final Pattern PRODUCT_ID_PATTERN = Pattern.compile("(?:productId|商品ID|商品编号|商品)[:：\\s-]*(\\d+)", Pattern.CASE_INSENSITIVE);

    private final AiPromptTemplateService promptTemplateService;
    private final AiModelClient aiModelClient;
    private final OrderRemoteLocalQuery orderRemoteLocalQuery;
    private final ProductFeignClient productFeignClient;
    private final StockFeignClient stockFeignClient;
    private final AiChatRecordMapper aiChatRecordMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiCustomerServiceImpl(AiPromptTemplateService promptTemplateService,
                                 AiModelClient aiModelClient,
                                 OrderRemoteLocalQuery orderRemoteLocalQuery,
                                 ProductFeignClient productFeignClient,
                                 StockFeignClient stockFeignClient,
                                 AiChatRecordMapper aiChatRecordMapper) {
        this.promptTemplateService = promptTemplateService;
        this.aiModelClient = aiModelClient;
        this.orderRemoteLocalQuery = orderRemoteLocalQuery;
        this.productFeignClient = productFeignClient;
        this.stockFeignClient = stockFeignClient;
        this.aiChatRecordMapper = aiChatRecordMapper;
    }

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        String userMessage = request == null ? "" : request.getMessage();
        if (!StringUtils.hasText(userMessage)) {
            return response("亲亲，请先输入要咨询的问题哦~", "other", Map.of(), false, "local-rule");
        }

        AiIntentResult intentResult = recognizeIntent(userMessage);
        AiChatResponse response = switch (intentResult.getIntent()) {
            case "order_logistics" -> handleOrderLogistics(userMessage, intentResult);
            case "goods_stock" -> handleGoodsStock(userMessage, intentResult);
            case "product_consult" -> handleProductConsult(userMessage, intentResult);
            default -> handleOther(userMessage, intentResult);
        };

        saveRecord(request == null ? null : request.getUserId(), userMessage, response.getReply());
        return response;
    }

    private AiIntentResult recognizeIntent(String userMessage) {
        if (aiModelClient.available()) {
            try {
                String content = aiModelClient.complete(promptTemplateService.buildIntentRecognitionPrompt(userMessage));
                return normalizeIntent(parseIntent(content), userMessage);
            } catch (Exception ignored) {
                return localRecognize(userMessage);
            }
        }
        return localRecognize(userMessage);
    }

    private AiChatResponse handleOrderLogistics(String userMessage, AiIntentResult intentResult) {
        String orderNo = slot(intentResult, "orderNo");
        if (!StringUtils.hasText(orderNo)) {
            return response("亲亲，请提供一下订单号哦~", "order_logistics", intentResult.getSlots(), false, "local-rule");
        }

        OrderMain order = orderRemoteLocalQuery.getOrderByOrderNo(orderNo);
        if (order == null) {
            return response("亲亲，暂时没有查询到这个订单，请核对订单号后再试哦~", "order_logistics", intentResult.getSlots(), false, "order_db");
        }

        String prompt = promptTemplateService.buildOrderLogisticsPrompt(userMessage, order);
        String fallback = fallbackOrderReply(order);
        String reply = completeOrFallback(prompt, fallback);
        return response(reply, "order_logistics", intentResult.getSlots(), true, "order_db");
    }

    private AiChatResponse handleGoodsStock(String userMessage, AiIntentResult intentResult) {
        Long productId = parseLong(slot(intentResult, "productId"));
        if (productId == null) {
            return response("亲亲，请提供一下商品ID，我才能帮你查询实时库存哦~", "goods_stock", intentResult.getSlots(), false, "local-rule");
        }

        RemoteProductDTO product = requireData(productFeignClient.detail(productId));
        List<RemoteStockDTO> stocks = requireData(stockFeignClient.listByProductId(productId));
        String prompt = promptTemplateService.buildStockPrompt(userMessage, product, stocks);
        String reply = completeOrFallback(prompt, fallbackStockReply(product, stocks));
        return response(reply, "goods_stock", intentResult.getSlots(), true, "product-center + stock-center");
    }

    private AiChatResponse handleProductConsult(String userMessage, AiIntentResult intentResult) {
        Long productId = parseLong(slot(intentResult, "productId"));
        if (productId == null) {
            String prompt = promptTemplateService.buildGeneralCustomerPrompt(userMessage);
            String reply = completeOrFallback(prompt, "亲亲，可以把商品ID发我一下，我再帮你查询商品详情哦~");
            return response(reply, "product_consult", intentResult.getSlots(), false, "local-rule");
        }

        RemoteProductDTO product = requireData(productFeignClient.detail(productId));
        String fallback = "亲亲，商品%s当前分类为%s，价格为%s，具体描述：%s。"
                .formatted(product.getProductName(), product.getCategory(), product.getPrice(), product.getDescription());
        String reply = completeOrFallback(promptTemplateService.buildGeneralCustomerPrompt(userMessage + "\n商品真实数据：" + fallback), fallback);
        return response(reply, "product_consult", intentResult.getSlots(), true, "product-center");
    }

    private AiChatResponse handleOther(String userMessage, AiIntentResult intentResult) {
        String replyTip = intentResult.getReplyTip();
        String fallback = StringUtils.hasText(replyTip) ? replyTip : "抱歉亲亲，我只能解答订单、物流、商品和库存相关的问题哦~";
        if (looksLikeShoppingQuestion(userMessage)) {
            fallback = completeOrFallback(promptTemplateService.buildGeneralCustomerPrompt(userMessage), fallback);
        }
        return response(fallback, "other", intentResult.getSlots(), false, "local-rule");
    }

    private AiIntentResult parseIntent(String content) throws Exception {
        String json = content == null ? "" : content.trim();
        if (json.startsWith("```")) {
            json = json.replaceFirst("^```json", "").replaceFirst("^```", "").replaceFirst("```$", "").trim();
        }
        JsonNode root = objectMapper.readTree(json);
        Map<String, String> slots = new LinkedHashMap<>();
        JsonNode slotsNode = root.path("slots");
        slotsNode.fields().forEachRemaining(entry -> {
            JsonNode value = entry.getValue();
            slots.put(entry.getKey(), value == null || value.isNull() ? null : value.asText());
        });
        return new AiIntentResult(
                root.path("intent").asText("other"),
                slots,
                root.path("is_complete").asBoolean(false),
                root.path("reply_tip").isNull() ? null : root.path("reply_tip").asText(null)
        );
    }

    private AiIntentResult normalizeIntent(AiIntentResult result, String userMessage) {
        Map<String, String> slots = new LinkedHashMap<>(result.getSlots());
        if (slots.containsKey("order_id") && !slots.containsKey("orderNo")) {
            slots.put("orderNo", slots.get("order_id"));
        }
        if (slots.containsKey("goods_id") && !slots.containsKey("productId")) {
            slots.put("productId", slots.get("goods_id"));
        }
        String intent = switch (result.getIntent()) {
            case "order_logistics", "goods_stock", "product_consult" -> result.getIntent();
            default -> "other";
        };
        AiIntentResult normalized = new AiIntentResult(intent, slots, result.isComplete(), result.getReplyTip());
        if (!normalized.isComplete()) {
            return normalized;
        }
        if ("order_logistics".equals(intent) && !StringUtils.hasText(slot(normalized, "orderNo"))) {
            normalized.setComplete(false);
            normalized.setReplyTip("亲亲，请提供一下订单号哦~");
        }
        if (("goods_stock".equals(intent) || "product_consult".equals(intent)) && !StringUtils.hasText(slot(normalized, "productId"))) {
            normalized.setComplete(false);
            normalized.setReplyTip("亲亲，请提供一下商品ID哦~");
        }
        return normalized;
    }

    private AiIntentResult localRecognize(String userMessage) {
        Map<String, String> slots = new LinkedHashMap<>();
        Matcher orderMatcher = ORDER_NO_PATTERN.matcher(userMessage);
        String orderNo = orderMatcher.find() ? orderMatcher.group() : null;
        Matcher productMatcher = PRODUCT_ID_PATTERN.matcher(userMessage);
        String productId = productMatcher.find() ? productMatcher.group(1) : null;

        if (containsAny(userMessage, "订单", "物流", "快递", "发货", "到哪", "送达")) {
            slots.put("orderNo", orderNo);
            return new AiIntentResult("order_logistics", slots, StringUtils.hasText(orderNo), StringUtils.hasText(orderNo) ? null : "亲亲，请提供一下订单号哦~");
        }
        if (containsAny(userMessage, "库存", "有货", "缺货", "库存量")) {
            slots.put("productId", productId);
            return new AiIntentResult("goods_stock", slots, StringUtils.hasText(productId), StringUtils.hasText(productId) ? null : "亲亲，请提供一下商品ID，我才能帮你查询实时库存哦~");
        }
        if (containsAny(userMessage, "商品", "价格", "规格", "参数")) {
            slots.put("productId", productId);
            return new AiIntentResult("product_consult", slots, StringUtils.hasText(productId), StringUtils.hasText(productId) ? null : "亲亲，请提供一下商品ID哦~");
        }
        return new AiIntentResult("other", slots, false, "抱歉亲亲，我只能解答购物相关的问题哦~");
    }

    private String completeOrFallback(String prompt, String fallback) {
        if (!aiModelClient.available()) {
            return fallback;
        }
        try {
            String reply = aiModelClient.complete(prompt);
            return StringUtils.hasText(reply) ? reply : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private String fallbackOrderReply(OrderMain order) {
        return "亲亲，订单 %s 当前%s，物流状态为%s，当前位置：%s，预计送达时间：%s。"
                .formatted(
                        order.getOrderNo(),
                        value(order.getOrderStatus()),
                        value(order.getLogisticsStatus()),
                        value(order.getCurrentLocation()),
                        value(order.getExpectArriveTime())
                );
    }

    private String fallbackStockReply(RemoteProductDTO product, List<RemoteStockDTO> stocks) {
        int total = stocks.stream()
                .map(RemoteStockDTO::getStockNum)
                .filter(value -> value != null)
                .mapToInt(Integer::intValue)
                .sum();
        String status = total > 0 ? "目前有货" : "当前缺货";
        return "亲亲，%s %s，可用库存合计 %d 件。".formatted(product.getProductName(), status, total);
    }

    private AiChatResponse response(String reply, String intent, Map<String, String> slots, boolean complete, String dataSource) {
        return new AiChatResponse(
                reply,
                intent,
                slots == null ? Map.of() : slots,
                complete,
                dataSource,
                aiModelClient.available() ? aiModelClient.modelName() : "local-fallback",
                LocalDateTime.now()
        );
    }

    private void saveRecord(Long userId, String question, String answer) {
        try {
            AiChatRecord record = new AiChatRecord();
            record.setUserId(userId);
            record.setQuestion(question);
            record.setAnswer(answer);
            record.setCreateTime(LocalDateTime.now());
            aiChatRecordMapper.insert(record);
        } catch (Exception ignored) {
            // AI 回复不应因为历史记录写入失败而中断。
        }
    }

    private String slot(AiIntentResult result, String name) {
        return result.getSlots() == null ? null : result.getSlots().get(name);
    }

    private Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private <T> T requireData(R<T> response) {
        if (response == null || response.getCode() == null || response.getCode() != 200 || response.getData() == null) {
            throw new IllegalStateException("远程服务调用失败");
        }
        return response.getData();
    }

    private boolean containsAny(String source, String... keywords) {
        for (String keyword : keywords) {
            if (source.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean looksLikeShoppingQuestion(String userMessage) {
        return containsAny(userMessage, "商品", "订单", "物流", "库存", "活动", "优惠", "发票", "支付");
    }

    private String value(Object value) {
        return value == null ? "暂未同步" : value.toString();
    }
}
