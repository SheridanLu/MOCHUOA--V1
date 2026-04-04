package com.mochu.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.common.constant.Constants;
import com.mochu.common.exception.BusinessException;
import com.mochu.common.result.PageResult;
import com.mochu.system.dto.UserCreateDTO;
import com.mochu.system.dto.UserQueryDTO;
import com.mochu.system.dto.UserUpdateDTO;
import com.mochu.system.entity.*;
import com.mochu.system.mapper.*;
import com.mochu.system.service.SysRoleService;
import com.mochu.system.service.SysUserService;
import com.mochu.system.vo.RoleVO;
import com.mochu.system.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysDepartmentMapper departmentMapper;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final SysRoleService roleService;

    @Override
    @Transactional(readOnly = true)
    public PageResult<UserVO> listUsers(UserQueryDTO query) {
        int page = query.getPage() != null ? query.getPage() : Constants.DEFAULT_PAGE;
        int size = Math.min(query.getSize() != null ? query.getSize() : Constants.DEFAULT_SIZE, Constants.MAX_SIZE);

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(SysUser::getUsername, query.getKeyword())
                    .or().like(SysUser::getRealName, query.getKeyword())
                    .or().like(SysUser::getPhone, query.getKeyword()));
        }
        if (query.getDeptId() != null) wrapper.eq(SysUser::getDeptId, query.getDeptId());
        if (query.getStatus() != null) wrapper.eq(SysUser::getStatus, query.getStatus());
        wrapper.orderByDesc(SysUser::getCreatedAt);

        IPage<SysUser> pageResult = userMapper.selectPage(new Page<>(page, size), wrapper);
        List<UserVO> voList = pageResult.getRecords().stream().map(this::toUserVO).collect(Collectors.toList());
        return new PageResult<>(voList, pageResult.getTotal(), page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public UserVO getUserById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(404, "用户不存在");
        return toUserVO(user);
    }

    @Override
    @Transactional
    public Long createUser(UserCreateDTO dto) {
        if (userMapper.selectByAccount(dto.getUsername()) != null) throw new BusinessException("用户名已存在");
        if (dto.getPhone() != null && userMapper.selectByAccount(dto.getPhone()) != null) throw new BusinessException("手机号已存在");

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setDeptId(dto.getDeptId());
        user.setStatus(1);
        userMapper.insert(user);

        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            roleService.assignRolesToUser(user.getId(), dto.getRoleIds());
        }
        return user.getId();
    }

    @Override
    @Transactional
    public void updateUser(Long id, UserUpdateDTO dto) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(404, "用户不存在");
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setDeptId(dto.getDeptId());
        userMapper.updateById(user);
        if (dto.getRoleIds() != null) roleService.assignRolesToUser(id, dto.getRoleIds());
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(404, "用户不存在");
        user.setStatus(status);
        userMapper.updateById(user);
        if (status == 0) {
            Set<String> keys = redisTemplate.keys(Constants.REDIS_TOKEN_PREFIX + id + ":*");
            if (keys != null && !keys.isEmpty()) redisTemplate.delete(keys);
            redisTemplate.delete(Constants.REDIS_PERMISSIONS_PREFIX + id);
        }
    }

    @Override
    @Transactional
    public void resetPassword(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(404, "用户不存在");
        user.setPasswordHash(passwordEncoder.encode("Mochu@2026"));
        userMapper.updateById(user);
        log.info("Password reset for user {} ({})", user.getUsername(), user.getPhone());
    }

    private UserVO toUserVO(SysUser user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setDeptId(user.getDeptId());
        vo.setStatus(user.getStatus());
        vo.setLastLoginAt(user.getLastLoginAt());
        vo.setCreatedAt(user.getCreatedAt());
        if (user.getDeptId() != null) {
            var dept = departmentMapper.selectById(user.getDeptId());
            if (dept != null) vo.setDeptName(dept.getDeptName());
        }
        List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());
        vo.setRoles(roles.stream().map(r -> {
            RoleVO rv = new RoleVO();
            rv.setId(r.getId());
            rv.setRoleCode(r.getRoleCode());
            rv.setRoleName(r.getRoleName());
            rv.setDataScope(r.getDataScope());
            rv.setStatus(r.getStatus());
            return rv;
        }).collect(Collectors.toList()));
        return vo;
    }
}
