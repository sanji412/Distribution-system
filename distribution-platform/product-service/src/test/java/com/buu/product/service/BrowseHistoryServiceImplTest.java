package com.buu.product.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.buu.product.entity.BrowseHistory;
import com.buu.product.mapper.BrowseHistoryMapper;
import com.buu.product.service.impl.BrowseHistoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BrowseHistoryServiceImplTest {

    @Mock
    private BrowseHistoryMapper browseHistoryMapper;

    @Test
    void listByUserIdQueriesBrowseHistory() {
        BrowseHistory history = new BrowseHistory();
        history.setId(1L);
        history.setUserId(1L);
        history.setProductId(2L);
        history.setBrowseTime(LocalDateTime.of(2025, 5, 18, 10, 15, 20));

        given(browseHistoryMapper.selectList(any())).willReturn(List.of(history));

        BrowseHistoryService service = new BrowseHistoryServiceImpl(browseHistoryMapper);
        List<BrowseHistory> result = service.listByUserId(1L);

        assertThat(result).containsExactly(history);
        ArgumentCaptor<Wrapper<BrowseHistory>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(browseHistoryMapper).selectList(wrapperCaptor.capture());
        assertThat(wrapperCaptor.getValue()).isNotNull();
    }
}
