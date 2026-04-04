package com.mochu.system.controller;

import com.mochu.common.result.R;
import com.mochu.system.service.SysPermissionService;
import com.mochu.system.vo.PermissionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class SysPermissionController {

    private final SysPermissionService permissionService;

    @GetMapping
    public R<List<PermissionVO>> list() { return R.ok(permissionService.listAll()); }
}
