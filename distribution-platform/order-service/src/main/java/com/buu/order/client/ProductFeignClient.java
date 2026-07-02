package com.buu.order.client;

import com.buu.order.common.R;
import com.buu.order.dto.RemoteBrowseHistoryDTO;
import com.buu.order.dto.RemoteProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 商品中心 Feign 客户端
 * 通过 Nacos 服务名调用 product-center 的商品查询接口。
 */
@FeignClient(name = "product-center")
public interface ProductFeignClient {

    /**
     * 查询商品详情
     *
     * @param productId 商品 ID
     * @return 商品中心返回的商品信息
     */
    @GetMapping("/api/product/{productId}")
    R<RemoteProductDTO> detail(@PathVariable("productId") Long productId);

    /**
     * 查询用户商品浏览历史
     *
     * @param userId 用户 ID
     * @return 商品中心返回的浏览历史列表
     */
    @GetMapping("/api/product/browse-history/{userId}")
    R<List<RemoteBrowseHistoryDTO>> listBrowseHistory(@PathVariable("userId") Long userId);
}
