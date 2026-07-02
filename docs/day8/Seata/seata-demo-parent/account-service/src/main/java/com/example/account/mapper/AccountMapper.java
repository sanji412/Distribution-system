package com.example.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.account.entity.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * 账户数据访问层
 */
@Mapper
public interface AccountMapper extends BaseMapper<Account> {

    /**
     * 扣减账户余额，校验余额充足
     * @param userId 用户ID
     * @param money 扣减金额
     * @return 影响行数
     */
    @Update("UPDATE t_account SET balance = balance - #{money} WHERE user_id = #{userId} AND balance >= #{money}")
    int decreaseBalance(@Param("userId") Long userId, @Param("money") BigDecimal money);
}