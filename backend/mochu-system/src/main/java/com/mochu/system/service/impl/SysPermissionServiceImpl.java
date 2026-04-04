package com.mochu.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mochu.system.entity.SysPermission;
import com.mochu.system.mapper.SysPermissionMapper;
import com.mochu.system.service.SysPermissionService;
import com.mochu.system.vo.PermissionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl implements SysPermissionService {

    private final SysPermissionMapper permissionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PermissionVO> listAll() {
        return permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>().orderByAsc(SysPermission::getModule, SysPermission::getPermCode))
                .stream().map(p -> {
                    PermissionVO vo = new PermissionVO();
                    vo.setId(p.getId());
                    vo.setPermCode(p.getPermCode());
                    vo.setPermName(p.getPermName());
                    vo.setModule(p.getModule());
                    return vo;
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> getPermCodesByUserId(Long userId) {
        return permissionMapper.selectPermCodesByUserId(userId);
    }
}
