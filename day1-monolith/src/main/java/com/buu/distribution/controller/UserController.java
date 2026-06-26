package com.buu.distribution.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.buu.distribution.common.ApiResponse;
import com.buu.distribution.entity.SysUser;
import com.buu.distribution.exception.BusinessException;
import com.buu.distribution.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final SysUserMapper sysUserMapper;

    @GetMapping("/list")
    public ApiResponse<List<SysUser>> list() {
        return ApiResponse.success(sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>().orderByAsc(SysUser::getUserId)));
    }

    @GetMapping("/{userId}")
    public ApiResponse<SysUser> get(@PathVariable Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return ApiResponse.success(user);
    }

    @PostMapping("/login")
    public ApiResponse<SysUser> login(@RequestBody Map<String, String> request) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.get("username"))
                .eq(SysUser::getStatus, 1));
        if (user == null) {
            throw new BusinessException("账号不存在或已禁用");
        }
        return ApiResponse.success(user);
    }
}
