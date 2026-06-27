package com.buu.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buu.product.entity.BrowseHistory;
import com.buu.product.mapper.BrowseHistoryMapper;
import com.buu.product.service.BrowseHistoryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 浏览历史服务实现
 * 按用户维度查询最近浏览记录，供 AI 推荐场景使用。
 */
@Service
public class BrowseHistoryServiceImpl implements BrowseHistoryService {

    private final BrowseHistoryMapper browseHistoryMapper;

    public BrowseHistoryServiceImpl(BrowseHistoryMapper browseHistoryMapper) {
        this.browseHistoryMapper = browseHistoryMapper;
    }

    /**
     * 查询指定用户的浏览记录
     *
     * @param userId 用户 ID
     * @return 浏览记录列表
     */
    @Override
    public List<BrowseHistory> listByUserId(Long userId) {
        LambdaQueryWrapper<BrowseHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrowseHistory::getUserId, userId)
                .orderByDesc(BrowseHistory::getBrowseTime);
        return browseHistoryMapper.selectList(wrapper);
    }
}
