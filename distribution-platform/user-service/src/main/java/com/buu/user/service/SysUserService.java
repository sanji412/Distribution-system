package com.buu.user.service;

import com.buu.user.entity.SysUser;

import java.util.List;

/**
 * 系统用户服务
 * 提供用户基础查询能力。
 */
public interface SysUserService {

    /**
     * 查询全部用户
     *
     * @return 用户列表
     */
    List<SysUser> listUsers();

    /**
     * 根据用户 ID 查询用户
     *
     * @param userId 用户 ID
     * @return 用户信息，未找到时返回 null
     */
    SysUser getById(Long userId);
}
