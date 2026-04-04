package com.mochu.business.supplier.controller;

import com.mochu.business.supplier.dto.SupplierCreateDTO;
import com.mochu.business.supplier.dto.SupplierQueryDTO;
import com.mochu.business.supplier.dto.SupplierUpdateDTO;
import com.mochu.business.supplier.service.BizSupplierService;
import com.mochu.business.supplier.vo.SupplierVO;
import com.mochu.common.result.PageResult;
import com.mochu.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class BizSupplierController {

    private final BizSupplierService supplierService;

    @GetMapping
    @PreAuthorize("hasAuthority('supplier:view')")
    public R<PageResult<SupplierVO>> list(SupplierQueryDTO query) { return R.ok(supplierService.listSuppliers(query)); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier:view')")
    public R<SupplierVO> getById(@PathVariable Long id) { return R.ok(supplierService.getSupplierById(id)); }

    @GetMapping("/enabled")
    @PreAuthorize("hasAuthority('supplier:view')")
    public R<List<SupplierVO>> listEnabled() { return R.ok(supplierService.listAllEnabled()); }

    @PostMapping
    @PreAuthorize("hasAuthority('supplier:create')")
    public R<Long> create(@Valid @RequestBody SupplierCreateDTO dto) { return R.ok(supplierService.createSupplier(dto)); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier:edit')")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody SupplierUpdateDTO dto) { supplierService.updateSupplier(id, dto); return R.ok(); }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('supplier:disable')")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) { supplierService.updateStatus(id, status); return R.ok(); }
}
