package com.mochu.system.controller;

import com.mochu.common.result.PageResult;
import com.mochu.common.result.R;
import com.mochu.system.dto.UserCreateDTO;
import com.mochu.system.dto.UserQueryDTO;
import com.mochu.system.dto.UserUpdateDTO;
import com.mochu.system.service.SysUserService;
import com.mochu.system.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('user:view')")
    public R<PageResult<UserVO>> list(UserQueryDTO query) { return R.ok(userService.listUsers(query)); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:view')")
    public R<UserVO> getById(@PathVariable Long id) { return R.ok(userService.getUserById(id)); }

    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    public R<Long> create(@Valid @RequestBody UserCreateDTO dto) { return R.ok(userService.createUser(dto)); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:edit')")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) { userService.updateUser(id, dto); return R.ok(); }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('user:disable')")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) { userService.updateStatus(id, status); return R.ok(); }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('user:edit')")
    public R<Void> resetPassword(@PathVariable Long id) { userService.resetPassword(id); return R.ok(); }
}
