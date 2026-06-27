package com.buu.stock.service.impl;

import com.buu.stock.entity.Warehouse;
import com.buu.stock.mapper.WarehouseMapper;
import com.buu.stock.service.WarehouseService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 仓库服务实现
 * 基于仓库 Mapper 提供仓库列表查询能力。
 */
@Service
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseMapper warehouseMapper;

    public WarehouseServiceImpl(WarehouseMapper warehouseMapper) {
        this.warehouseMapper = warehouseMapper;
    }

    /**
     * 查询全部仓库
     *
     * @return 仓库列表
     */
    @Override
    public List<Warehouse> listWarehouses() {
        return warehouseMapper.selectList(null);
    }
}
