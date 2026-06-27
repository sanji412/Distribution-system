package com.buu.stock.service;

import com.buu.stock.entity.Warehouse;

import java.util.List;

/**
 * 仓库服务
 * 提供仓库基础查询能力。
 */
public interface WarehouseService {

    /**
     * 查询全部仓库
     *
     * @return 仓库列表
     */
    List<Warehouse> listWarehouses();
}
