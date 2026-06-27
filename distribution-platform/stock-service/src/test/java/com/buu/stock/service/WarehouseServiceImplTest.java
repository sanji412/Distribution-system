package com.buu.stock.service;

import com.buu.stock.entity.Warehouse;
import com.buu.stock.mapper.WarehouseMapper;
import com.buu.stock.service.impl.WarehouseServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceImplTest {

    @Mock
    private WarehouseMapper warehouseMapper;

    @Test
    void listWarehousesQueriesAllWarehouses() {
        Warehouse warehouse = new Warehouse();
        warehouse.setWarehouseId(1L);
        warehouse.setWarehouseName("北京顺义仓");

        given(warehouseMapper.selectList(null)).willReturn(List.of(warehouse));

        WarehouseService service = new WarehouseServiceImpl(warehouseMapper);
        List<Warehouse> result = service.listWarehouses();

        assertThat(result).containsExactly(warehouse);
        verify(warehouseMapper).selectList(null);
    }
}
