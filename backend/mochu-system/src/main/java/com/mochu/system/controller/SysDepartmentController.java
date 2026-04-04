package com.mochu.system.controller;

import com.mochu.common.result.R;
import com.mochu.system.dto.DeptCreateDTO;
import com.mochu.system.service.SysDepartmentService;
import com.mochu.system.vo.DeptTreeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class SysDepartmentController {

    private final SysDepartmentService departmentService;

    @GetMapping("/tree")
    public R<List<DeptTreeVO>> tree() { return R.ok(departmentService.getDeptTree()); }

    @PostMapping
    @PreAuthorize("hasAuthority('dept:create')")
    public R<Long> create(@Valid @RequestBody DeptCreateDTO dto) { return R.ok(departmentService.createDept(dto)); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('dept:edit')")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody DeptCreateDTO dto) { departmentService.updateDept(id, dto); return R.ok(); }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('dept:edit')")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) { departmentService.updateStatus(id, status); return R.ok(); }
}
