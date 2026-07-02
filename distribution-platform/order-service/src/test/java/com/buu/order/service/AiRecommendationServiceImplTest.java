package com.buu.order.service;

import com.buu.order.client.ProductFeignClient;
import com.buu.order.common.R;
import com.buu.order.dto.AiRecommendationResponse;
import com.buu.order.dto.RemoteBrowseHistoryDTO;
import com.buu.order.dto.RemoteProductDTO;
import com.buu.order.service.impl.AiRecommendationServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AiRecommendationServiceImplTest {

    @Test
    void recommendationsUseBrowseHistoryProductDetailsAndDeepSeekCards() {
        AiRecommendationService service = new AiRecommendationServiceImpl(
                new FakeProductFeignClient(),
                new FakeAiModelClient("""
                        [
                          {"title":"搭配推荐","content":"你最近浏览了机械键盘，建议搭配 USB-C 扩展坞提升桌面连接能力。"},
                          {"title":"库存提醒","content":"无线鼠标库存偏低，适合尽快下单避免缺货。"}
                        ]
                        """)
        );

        AiRecommendationResponse response = service.recommendForUser(1L);

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getDataSource()).isEqualTo("product-center browse_history");
        assertThat(response.getModelUsed()).isEqualTo("deepseek-chat");
        assertThat(response.getItems())
                .extracting(AiRecommendationResponse.Item::getTitle)
                .containsExactly("搭配推荐", "库存提醒");
        assertThat(response.getItems().getFirst().getContent())
                .contains("机械键盘")
                .contains("USB-C");
    }

    private static class FakeAiModelClient implements AiModelClient {

        private final String reply;

        private FakeAiModelClient(String reply) {
            this.reply = reply;
        }

        @Override
        public String complete(String prompt) {
            assertThat(prompt)
                    .contains("机械键盘-Keychron K3")
                    .contains("无线鼠标-罗技M720")
                    .contains("浏览历史");
            return reply;
        }

        @Override
        public boolean available() {
            return true;
        }

        @Override
        public String modelName() {
            return "deepseek-chat";
        }
    }

    private static class FakeProductFeignClient implements ProductFeignClient {

        @Override
        public R<RemoteProductDTO> detail(Long productId) {
            RemoteProductDTO product = new RemoteProductDTO();
            product.setProductId(productId);
            if (productId == 1L) {
                product.setProductName("机械键盘-Keychron K3");
                product.setCategory("外设");
                product.setPrice(new BigDecimal("368.00"));
                product.setDescription("轻薄机械键盘");
                return R.success(product);
            }
            product.setProductName("无线鼠标-罗技M720");
            product.setCategory("外设");
            product.setPrice(new BigDecimal("128.40"));
            product.setDescription("办公无线鼠标");
            return R.success(product);
        }

        @Override
        public R<List<RemoteBrowseHistoryDTO>> listBrowseHistory(Long userId) {
            RemoteBrowseHistoryDTO first = new RemoteBrowseHistoryDTO();
            first.setUserId(userId);
            first.setProductId(1L);
            first.setBrowseTime(LocalDateTime.of(2026, 7, 2, 10, 0));

            RemoteBrowseHistoryDTO second = new RemoteBrowseHistoryDTO();
            second.setUserId(userId);
            second.setProductId(2L);
            second.setBrowseTime(LocalDateTime.of(2026, 7, 2, 11, 0));

            return R.success(List.of(first, second));
        }
    }
}
