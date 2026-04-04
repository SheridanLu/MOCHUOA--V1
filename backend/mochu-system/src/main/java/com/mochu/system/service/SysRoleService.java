package com.mochu.system.service;

import com.mochu.system.dto.RoleCreateDTO;
import com.mochu.system.vo.RoleVO;
import java.util.List;

public interface SysRoleService {
    List<RoleVO> listRoles();
    RoleVO getRoleById(Long id);
    Long createRole(RoleCreateDTO dto);
    void updateRole(Long id, RoleCreateDTO dto);
    void assignRolesToUser(Long userId, List<Long> roleIds);
}
