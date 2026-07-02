package com.example.storage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.storage.entity.Storage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 库存数据访问层
 * 继承BaseMapper获得基础CRUD能力
 */
@Mapper
public interface StorageMapper extends BaseMapper<Storage> {

    /**
     * 扣减库存：增加已用库存，校验库存充足
     * @param productId 商品ID
     * @param count 扣减数量
     * @return 影响行数
     */
    @Update("UPDATE t_storage SET used = used + #{count} WHERE product_id = #{productId} AND total - used >= #{count}")
    int decreaseStorage(@Param("productId") Long productId, @Param("count") Integer count);
}