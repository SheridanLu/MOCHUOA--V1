package com.mochu.business.material.controller;

import com.mochu.business.material.dto.InboundCreateDTO;
import com.mochu.business.material.dto.MaterialQueryDTO;
import com.mochu.business.material.dto.OutboundCreateDTO;
import com.mochu.business.material.dto.ReturnCreateDTO;
import com.mochu.business.material.service.BizMaterialService;
import com.mochu.business.material.vo.*;
import com.mochu.common.result.PageResult;
import com.mochu.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/materials")
@RequiredArgsConstructor
public class BizMaterialController {

    private final BizMaterialService materialService;

    // ==================== INBOUND ====================

    @GetMapping("/inbounds")
    @PreAuthorize("hasAuthority('material:view')")
    public R<PageResult<InboundVO>> listInbounds(MaterialQueryDTO query) {
        return R.ok(materialService.listInbounds(query));
    }

    @GetMapping("/inbounds/{id}")
    @PreAuthorize("hasAuthority('material:view')")
    public R<InboundVO> getInbound(@PathVariable Long id) {
        return R.ok(materialService.getInboundById(id));
    }

    @PostMapping("/inbounds")
    @PreAuthorize("hasAuthority('material:create')")
    public R<Long> createInbound(@RequestBody InboundCreateDTO dto) {
        return R.ok(materialService.createInbound(dto));
    }

    @PostMapping("/inbounds/{id}/submit")
    @PreAuthorize("hasAuthority('material:create')")
    public R<Void> submitInbound(@PathVariable Long id) {
        materialService.submitInbound(id);
        return R.ok();
    }

    @PostMapping("/inbounds/{id}/approve")
    @PreAuthorize("hasAuthority('material:approve')")
    public R<Void> approveInbound(@PathVariable Long id, @RequestParam String comment) {
        materialService.approveInbound(id, comment);
        return R.ok();
    }

    @PostMapping("/inbounds/{id}/reject")
    @PreAuthorize("hasAuthority('material:approve')")
    public R<Void> rejectInbound(@PathVariable Long id, @RequestParam String comment) {
        materialService.rejectInbound(id, comment);
        return R.ok();
    }

    // ==================== OUTBOUND ====================

    @GetMapping("/outbounds")
    @PreAuthorize("hasAuthority('material:view')")
    public R<PageResult<OutboundVO>> listOutbounds(MaterialQueryDTO query) {
        return R.ok(materialService.listOutbounds(query));
    }

    @GetMapping("/outbounds/{id}")
    @PreAuthorize("hasAuthority('material:view')")
    public R<OutboundVO> getOutbound(@PathVariable Long id) {
        return R.ok(materialService.getOutboundById(id));
    }

    @PostMapping("/outbounds")
    @PreAuthorize("hasAuthority('material:create')")
    public R<Long> createOutbound(@RequestBody OutboundCreateDTO dto) {
        return R.ok(materialService.createOutbound(dto));
    }

    @PostMapping("/outbounds/{id}/submit")
    @PreAuthorize("hasAuthority('material:create')")
    public R<Void> submitOutbound(@PathVariable Long id) {
        materialService.submitOutbound(id);
        return R.ok();
    }

    @PostMapping("/outbounds/{id}/approve")
    @PreAuthorize("hasAuthority('material:approve')")
    public R<Void> approveOutbound(@PathVariable Long id, @RequestParam String comment) {
        materialService.approveOutbound(id, comment);
        return R.ok();
    }

    @PostMapping("/outbounds/{id}/reject")
    @PreAuthorize("hasAuthority('material:approve')")
    public R<Void> rejectOutbound(@PathVariable Long id, @RequestParam String comment) {
        materialService.rejectOutbound(id, comment);
        return R.ok();
    }

    // ==================== RETURN ====================

    @GetMapping("/returns")
    @PreAuthorize("hasAuthority('material:view')")
    public R<PageResult<ReturnVO>> listReturns(MaterialQueryDTO query) {
        return R.ok(materialService.listReturns(query));
    }

    @PostMapping("/returns")
    @PreAuthorize("hasAuthority('material:create')")
    public R<Long> createReturn(@RequestBody ReturnCreateDTO dto) {
        return R.ok(materialService.createReturn(dto));
    }

    @PostMapping("/returns/{id}/approve")
    @PreAuthorize("hasAuthority('material:approve')")
    public R<Void> approveReturn(@PathVariable Long id, @RequestParam String comment) {
        materialService.approveReturn(id, comment);
        return R.ok();
    }

    // ==================== INVENTORY ====================

    @GetMapping("/inventory")
    @PreAuthorize("hasAuthority('material:view')")
    public R<List<InventoryVO>> queryInventory(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String warehouse) {
        return R.ok(materialService.queryInventory(projectId, keyword, warehouse));
    }
}
