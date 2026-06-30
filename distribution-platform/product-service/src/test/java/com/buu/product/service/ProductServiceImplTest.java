package com.buu.product.service;

import com.buu.product.entity.Product;
import com.buu.product.mapper.ProductMapper;
import com.buu.product.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductMapper productMapper;

    @Test
    void createProductInsertsProductWithTimestamps() {
        Product product = new Product();
        product.setProductName("机械键盘-Keychron K3");
        product.setCategory("外设");
        product.setPrice(new BigDecimal("368.00"));
        product.setSkuCode("SKU-K3-001");
        product.setStatus(1);

        given(productMapper.insert(product)).willReturn(1);

        ProductService service = new ProductServiceImpl(productMapper);
        Product result = service.createProduct(product);

        assertThat(result).isSameAs(product);
        assertThat(result.getCreateTime()).isNotNull();
        assertThat(result.getUpdateTime()).isNotNull();
        verify(productMapper).insert(product);
    }

    @Test
    void updateProductSetsIdAndUpdateTimeBeforeSaving() {
        Product product = new Product();
        product.setProductName("无线鼠标-罗技M720");
        product.setPrice(new BigDecimal("128.40"));

        given(productMapper.updateById(product)).willReturn(1);

        ProductService service = new ProductServiceImpl(productMapper);
        Product result = service.updateProduct(2L, product);

        assertThat(result).isSameAs(product);
        assertThat(result.getProductId()).isEqualTo(2L);
        assertThat(result.getUpdateTime()).isNotNull();
        verify(productMapper).updateById(product);
    }

    @Test
    void deleteProductRemovesProductById() {
        given(productMapper.deleteById(3L)).willReturn(1);

        ProductService service = new ProductServiceImpl(productMapper);
        boolean deleted = service.deleteProduct(3L);

        assertThat(deleted).isTrue();
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(productMapper).deleteById(idCaptor.capture());
        assertThat(idCaptor.getValue()).isEqualTo(3L);
    }
}
