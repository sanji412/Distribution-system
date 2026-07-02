package com.buu.order.service.impl;

import com.buu.order.client.ProductFeignClient;
import com.buu.order.common.R;
import com.buu.order.dto.AiRecommendationResponse;
import com.buu.order.dto.RemoteBrowseHistoryDTO;
import com.buu.order.dto.RemoteProductDTO;
import com.buu.order.service.AiModelClient;
import com.buu.order.service.AiRecommendationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AI 智能推荐服务实现
 * order-center 通过 OpenFeign 读取 product-center 浏览历史，再拼接 Prompt 生成推荐语。
 */
@Service
public class AiRecommendationServiceImpl implements AiRecommendationService {

    private static final int MAX_HISTORY_PRODUCTS = 4;
    private static final int MAX_RECOMMENDATIONS = 3;

    private final ProductFeignClient productFeignClient;
    private final AiModelClient aiModelClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiRecommendationServiceImpl(ProductFeignClient productFeignClient, AiModelClient aiModelClient) {
        this.productFeignClient = productFeignClient;
        this.aiModelClient = aiModelClient;
    }

    @Override
    public AiRecommendationResponse recommendForUser(Long userId) {
        Long targetUserId = userId == null ? 1L : userId;
        List<RemoteBrowseHistoryDTO> histories = requireData(productFeignClient.listBrowseHistory(targetUserId));
        List<RemoteProductDTO> products = loadHistoryProducts(histories);
        List<AiRecommendationResponse.Item> items = buildRecommendations(targetUserId, histories, products);
        return new AiRecommendationResponse(
                targetUserId,
                "product-center browse_history",
                aiModelClient.available() ? aiModelClient.modelName() : "local-fallback",
                LocalDateTime.now(),
                items
        );
    }

    private List<RemoteProductDTO> loadHistoryProducts(List<RemoteBrowseHistoryDTO> histories) {
        Map<Long, RemoteProductDTO> products = new LinkedHashMap<>();
        for (RemoteBrowseHistoryDTO history : histories) {
            if (history.getProductId() == null || products.containsKey(history.getProductId())) {
                continue;
            }
            RemoteProductDTO product = requireData(productFeignClient.detail(history.getProductId()));
            products.put(history.getProductId(), product);
            if (products.size() >= MAX_HISTORY_PRODUCTS) {
                break;
            }
        }
        return new ArrayList<>(products.values());
    }

    private List<AiRecommendationResponse.Item> buildRecommendations(Long userId,
                                                                      List<RemoteBrowseHistoryDTO> histories,
                                                                      List<RemoteProductDTO> products) {
        List<AiRecommendationResponse.Item> fallback = fallbackItems(products);
        if (!aiModelClient.available() || products.isEmpty()) {
            return fallback;
        }
        try {
            String reply = aiModelClient.complete(buildPrompt(userId, histories, products));
            List<AiRecommendationResponse.Item> parsed = parseItems(reply);
            return parsed.isEmpty() ? fallback : parsed;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private String buildPrompt(Long userId,
                               List<RemoteBrowseHistoryDTO> histories,
                               List<RemoteProductDTO> products) {
        StringBuilder productLines = new StringBuilder();
        for (RemoteProductDTO product : products) {
            productLines.append("- 商品ID：")
                    .append(value(product.getProductId()))
                    .append("，名称：")
                    .append(value(product.getProductName()))
                    .append("，分类：")
                    .append(value(product.getCategory()))
                    .append("，价格：")
                    .append(value(product.getPrice()))
                    .append("，描述：")
                    .append(value(product.getDescription()))
                    .append('\n');
        }

        StringBuilder historyLines = new StringBuilder();
        for (RemoteBrowseHistoryDTO history : histories) {
            historyLines.append("- 商品ID：")
                    .append(value(history.getProductId()))
                    .append("，浏览时间：")
                    .append(value(history.getBrowseTime()))
                    .append('\n');
        }

        return """
                【角色人设】
                你是优选电商的智能导购助手，语气亲切、简洁、专业。

                【任务】
                基于用户浏览历史和商品真实数据，生成 2-3 条智能推荐卡片。

                【硬性要求】
                1. 只能依据下方浏览历史和商品数据推荐，禁止编造不存在的商品
                2. 每条推荐要有明确标题和一句推荐理由
                3. 严格输出 JSON 数组，不要 Markdown，不要解释说明
                4. JSON 格式为：[{"title":"标题","content":"推荐理由"}]

                【用户ID】
                %s

                【浏览历史】
                %s

                【商品真实数据】
                %s
                """.formatted(userId, historyLines, productLines);
    }

    private List<AiRecommendationResponse.Item> parseItems(String content) throws Exception {
        String json = normalizeJson(content);
        JsonNode root = objectMapper.readTree(json);
        JsonNode itemsNode = root.isArray() ? root : root.path("items");
        List<AiRecommendationResponse.Item> items = new ArrayList<>();
        if (!itemsNode.isArray()) {
            return items;
        }
        for (JsonNode node : itemsNode) {
            String title = node.path("title").asText("");
            String itemContent = node.path("content").asText("");
            if (!StringUtils.hasText(title) || !StringUtils.hasText(itemContent)) {
                continue;
            }
            items.add(new AiRecommendationResponse.Item(title, itemContent));
            if (items.size() >= MAX_RECOMMENDATIONS) {
                break;
            }
        }
        return items;
    }

    private String normalizeJson(String content) {
        String json = Objects.toString(content, "").trim();
        if (json.startsWith("```")) {
            json = json.replaceFirst("^```json", "")
                    .replaceFirst("^```", "")
                    .replaceFirst("```$", "")
                    .trim();
        }
        return json;
    }

    private List<AiRecommendationResponse.Item> fallbackItems(List<RemoteProductDTO> products) {
        if (products.isEmpty()) {
            return List.of(new AiRecommendationResponse.Item(
                    "暂无浏览历史",
                    "亲亲，暂时还没有浏览记录，先看看商品后我就能给你生成个性化推荐。"
            ));
        }

        List<AiRecommendationResponse.Item> items = new ArrayList<>();
        RemoteProductDTO first = products.getFirst();
        items.add(new AiRecommendationResponse.Item(
                "搭配推荐",
                "你最近浏览了%s，可以优先关注同类%s商品，适合一起加入采购清单。"
                        .formatted(value(first.getProductName()), value(first.getCategory()))
        ));

        if (products.size() > 1) {
            RemoteProductDTO second = products.get(1);
            items.add(new AiRecommendationResponse.Item(
                    "对比选择",
                    "%s 与 %s 都属于近期关注商品，可以按价格和使用场景做组合选择。"
                            .formatted(value(first.getProductName()), value(second.getProductName()))
            ));
        }

        items.add(new AiRecommendationResponse.Item(
                "导购提醒",
                "推荐语基于 product-center 浏览历史生成，继续浏览商品后推荐会更加贴合。"
        ));
        return items.size() > MAX_RECOMMENDATIONS ? items.subList(0, MAX_RECOMMENDATIONS) : items;
    }

    private <T> T requireData(R<T> response) {
        if (response == null || response.getCode() == null || response.getCode() != 200 || response.getData() == null) {
            throw new IllegalStateException("商品中心远程调用失败");
        }
        return response.getData();
    }

    private String value(Object value) {
        return Objects.toString(value, "暂未同步");
    }
}
