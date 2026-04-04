package com.mochu.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mochu.common.constant.Constants;
import com.mochu.common.exception.BusinessException;
import com.mochu.system.dto.RoleCreateDTO;
import com.mochu.system.entity.*;
import com.mochu.system.mapper.*;
import com.mochu.system.service.SysRoleService;
import com.mochu.system.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional(readOnly = true)
    public List<RoleVO> listRoles() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getId))
                .stream().map(this::toRoleVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleVO getRoleById(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) throw new BusinessException(404, "角色不存在");
        return toRoleVO(role);
    }

    @Override
    @Transactional
    public Long createRole(RoleCreateDTO dto) {
        if (roleMapper.selectCount(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, dto.getRoleCode())) > 0) {
            throw new BusinessException("角色编码已存在");
        }
        SysRole role = new SysRole();
        role.setRoleCode(dto.getRoleCode());
        role.setRoleName(dto.getRoleName());
        role.setDescription(dto.getDescription());
        role.setDataScope(dto.getDataScope());
        role.setStatus(1);
        roleMapper.insert(role);
        if (dto.getPermissionIds() != null) saveRolePermissions(role.getId(), dto.getPermissionIds());
        return role.getId();
    }

    @Override
    @Transactional
    public void updateRole(Long id, RoleCreateDTO dto) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) throw new BusinessException(404, "角色不存在");
        role.setRoleName(dto.getRoleName());
        role.setDescription(dto.getDescription());
        role.setDataScope(dto.getDataScope());
        roleMapper.updateById(role);
        if (dto.getPermissionIds() != null) {
            rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, id));
            saveRolePermissions(id, dto.getPermissionIds());
        }
        clearAffectedUsersCache(id);
    }

    @Override
    @Transactional
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        checkMutexRoles(roleIds);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        for (Long roleId : roleIds) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            ur.setCreatedAt(LocalDateTime.now());
            userRoleMapper.insert(ur);
        }
        redisTemplate.delete(Constants.REDIS_PERMISSIONS_PREFIX + userId);
    }

    private void checkMutexRoles(List<Long> roleIds) {
        if (roleIds.size() < 2) return;
        List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
        Set<String> codes = roles.stream().map(SysRole::getRoleCode).collect(Collectors.toSet());
        if (codes.contains("PURCHASE") && codes.contains("FINANCE")) {
            throw new BusinessException("采购员和财务人员角色互斥，不能同时分配");
        }
    }

    private void saveRolePermissions(Long roleId, List<Long> permissionIds) {
        for (Long permId : permissionIds) {
            SysRolePermission rp = new SysRolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permId);
            rp.setCreatedAt(LocalDateTime.now());
            rolePermissionMapper.insert(rp);
        }
    }

    private void clearAffectedUsersCache(Long roleId) {
        userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId))
                .forEach(ur -> redisTemplate.delete(Constants.REDIS_PERMISSIONS_PREFIX + ur.getUserId()));
    }

    private RoleVO toRoleVO(SysRole role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getRoleName());
        vo.setDescription(role.getDescription());
        vo.setDataScope(role.getDataScope());
        vo.setStatus(role.getStatus());
        vo.setPermissionIds(rolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, role.getId()))
                .stream().map(SysRolePermission::getPermissionId).collect(Collectors.toList()));
        return vo;
    }
}
