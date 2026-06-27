package com.buu.user.controller;

import com.buu.user.common.R;
import com.buu.user.entity.SysUser;
import com.buu.user.service.SysUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户 Controller
 * 处理用户中心的基础查询请求。
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final SysUserService sysUserService;

    public UserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    /**
     * 查询全部用户
     *
     * @return 用户列表
     */
    @GetMapping("/list")
    public R<List<SysUser>> list() {
        return R.success(sysUserService.listUsers());
    }

    /**
     * 根据用户 ID 查询用户
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    @GetMapping("/{userId}")
    public R<SysUser> detail(@PathVariable Long userId) {
        return R.success(sysUserService.getById(userId));
    }
}
