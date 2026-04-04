package com.mochu.business.change.controller;

import com.mochu.business.change.dto.*;
import com.mochu.business.change.service.BizChangeService;
import com.mochu.business.change.vo.*;
import com.mochu.common.result.PageResult;
import com.mochu.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/changes")
@RequiredArgsConstructor
public class BizChangeController {

    private final BizChangeService changeService;

    // ==================== SITE VISA ====================

    @PostMapping("/site-visas")
    @PreAuthorize("hasAuthority('change:create')")
    public R<Long> createSiteVisa(@Valid @RequestBody SiteVisaCreateDTO dto) {
        return R.ok(changeService.createSiteVisa(dto));
    }

    @PostMapping("/site-visas/{id}/approve")
    @PreAuthorize("hasAuthority('change:approve')")
    public R<Void> approveSiteVisa(@PathVariable Long id, @RequestParam String comment) {
        changeService.approveSiteVisa(id, comment);
        return R.ok();
    }

    @PostMapping("/site-visas/{id}/reject")
    @PreAuthorize("hasAuthority('change:approve')")
    public R<Void> rejectSiteVisa(@PathVariable Long id, @RequestParam String comment) {
        changeService.rejectSiteVisa(id, comment);
        return R.ok();
    }

    // ==================== OWNER CHANGE ====================

    @PostMapping("/owner-changes")
    @PreAuthorize("hasAuthority('change:create')")
    public R<Long> createOwnerChange(@Valid @RequestBody OwnerChangeCreateDTO dto) {
        return R.ok(changeService.createOwnerChange(dto));
    }

    @PostMapping("/owner-changes/{id}/approve")
    @PreAuthorize("hasAuthority('change:approve')")
    public R<Void> approveOwnerChange(@PathVariable Long id, @RequestParam String comment) {
        changeService.approveOwnerChange(id, comment);
        return R.ok();
    }

    @PostMapping("/owner-changes/{id}/reject")
    @PreAuthorize("hasAuthority('change:approve')")
    public R<Void> rejectOwnerChange(@PathVariable Long id, @RequestParam String comment) {
        changeService.rejectOwnerChange(id, comment);
        return R.ok();
    }

    // ==================== LABOR VISA ====================

    @PostMapping("/labor-visas")
    @PreAuthorize("hasAuthority('change:create')")
    public R<Long> createLaborVisa(@Valid @RequestBody LaborVisaCreateDTO dto) {
        return R.ok(changeService.createLaborVisa(dto));
    }

    @PostMapping("/labor-visas/{id}/approve")
    @PreAuthorize("hasAuthority('change:approve')")
    public R<Void> approveLaborVisa(@PathVariable Long id, @RequestParam String comment) {
        changeService.approveLaborVisa(id, comment);
        return R.ok();
    }

    @PostMapping("/labor-visas/{id}/reject")
    @PreAuthorize("hasAuthority('change:approve')")
    public R<Void> rejectLaborVisa(@PathVariable Long id, @RequestParam String comment) {
        changeService.rejectLaborVisa(id, comment);
        return R.ok();
    }

    // ==================== CHANGE LEDGER ====================

    @GetMapping("/ledger")
    @PreAuthorize("hasAuthority('change:view')")
    public R<PageResult<ChangeLedgerVO>> queryChangeLedger(ChangeQueryDTO query) {
        return R.ok(changeService.queryChangeLedger(query));
    }
}
