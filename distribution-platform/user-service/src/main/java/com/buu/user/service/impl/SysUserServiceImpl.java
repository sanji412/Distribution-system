package com.buu.user.service.impl;

import com.buu.user.entity.SysUser;
import com.buu.user.mapper.SysUserMapper;
import com.buu.user.service.SysUserService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统用户服务实现
 * 基于用户 Mapper 提供用户查询能力。
 */
@Service
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;

    public SysUserServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    /**
     * 查询全部用户
     *
     * @return 用户列表
     */
    @Override
    public List<SysUser> listUsers() {
        return sysUserMapper.selectList(null);
    }

    /**
     * 根据用户 ID 查询用户
     *
     * @param userId 用户 ID
     * @return 用户信息，未找到时返回 null
     */
    @Override
    public SysUser getById(Long userId) {
        return sysUserMapper.selectById(userId);
    }
}
