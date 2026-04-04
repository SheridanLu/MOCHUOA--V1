package com.mochu.system.controller;

import com.mochu.common.result.R;
import com.mochu.system.dto.RoleCreateDTO;
import com.mochu.system.service.SysRoleService;
import com.mochu.system.vo.RoleVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('role:create') or hasAuthority('role:edit')")
    public R<List<RoleVO>> list() { return R.ok(roleService.listRoles()); }

    @GetMapping("/{id}")
    public R<RoleVO> getById(@PathVariable Long id) { return R.ok(roleService.getRoleById(id)); }

    @PostMapping
    @PreAuthorize("hasAuthority('role:create')")
    public R<Long> create(@Valid @RequestBody RoleCreateDTO dto) { return R.ok(roleService.createRole(dto)); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role:edit')")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody RoleCreateDTO dto) { roleService.updateRole(id, dto); return R.ok(); }

    @PostMapping("/assign/{userId}")
    @PreAuthorize("hasAuthority('user:edit')")
    public R<Void> assignRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) { roleService.assignRolesToUser(userId, roleIds); return R.ok(); }
}
