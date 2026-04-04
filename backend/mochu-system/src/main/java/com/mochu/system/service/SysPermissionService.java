package com.mochu.system.service;

import com.mochu.system.vo.PermissionVO;
import java.util.List;
import java.util.Set;

public interface SysPermissionService {
    List<PermissionVO> listAll();
    Set<String> getPermCodesByUserId(Long userId);
}
