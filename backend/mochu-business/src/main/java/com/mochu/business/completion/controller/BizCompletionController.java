package com.mochu.business.completion.controller;

import com.mochu.business.completion.dto.*;
import com.mochu.business.completion.service.BizCompletionService;
import com.mochu.business.completion.vo.*;
import com.mochu.common.result.PageResult;
import com.mochu.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/completion")
@RequiredArgsConstructor
public class BizCompletionController {

    private final BizCompletionService completionService;

    // ==================== COMPLETION REPORT ====================

    @GetMapping("/reports")
    @PreAuthorize("hasAuthority('completion:view')")
    public R<PageResult<CompletionReportVO>> listReports(CompletionQueryDTO query) {
        return R.ok(completionService.listReports(query));
    }

    @PostMapping("/reports")
    @PreAuthorize("hasAuthority('completion:create')")
    public R<Long> createReport(@Valid @RequestBody CompletionReportDTO dto) {
        return R.ok(completionService.createCompletionReport(dto));
    }

    @PostMapping("/reports/{id}/approve")
    @PreAuthorize("hasAuthority('completion:approve')")
    public R<Void> approveReport(@PathVariable Long id, @RequestParam String comment) {
        completionService.approveReport(id, comment);
        return R.ok();
    }

    @PostMapping("/reports/{id}/reject")
    @PreAuthorize("hasAuthority('completion:approve')")
    public R<Void> rejectReport(@PathVariable Long id, @RequestParam String comment) {
        completionService.rejectReport(id, comment);
        return R.ok();
    }

    // ==================== DRAWINGS ====================

    @GetMapping("/drawings")
    @PreAuthorize("hasAuthority('completion:view')")
    public R<List<DrawingVO>> listDrawings(@RequestParam Long projectId) {
        return R.ok(completionService.listDrawings(projectId));
    }

    @PostMapping("/drawings")
    @PreAuthorize("hasAuthority('completion:create')")
    public R<Void> uploadDrawings(@Valid @RequestBody DrawingUploadDTO dto) {
        completionService.uploadDrawings(dto);
        return R.ok();
    }

    @PostMapping("/drawings/{id}/version")
    @PreAuthorize("hasAuthority('completion:create')")
    public R<Void> uploadNewVersion(@PathVariable Long id, @RequestParam String fileUrl) {
        completionService.uploadNewVersion(id, fileUrl);
        return R.ok();
    }

    @PostMapping("/drawings/{id}/approve")
    @PreAuthorize("hasAuthority('completion:approve')")
    public R<Void> approveDrawing(@PathVariable Long id, @RequestParam String comment) {
        completionService.approveDrawing(id, comment);
        return R.ok();
    }

    @PostMapping("/drawings/{id}/reject")
    @PreAuthorize("hasAuthority('completion:approve')")
    public R<Void> rejectDrawing(@PathVariable Long id, @RequestParam String comment) {
        completionService.rejectDrawing(id, comment);
        return R.ok();
    }

    // ==================== ARCHIVE ====================

    @GetMapping("/archive")
    @PreAuthorize("hasAuthority('completion:view')")
    public R<ArchiveVO> getArchive(@RequestParam Long projectId) {
        return R.ok(completionService.getArchive(projectId));
    }

    // ==================== SETTLEMENT ====================

    @GetMapping("/settlement")
    @PreAuthorize("hasAuthority('completion:view')")
    public R<SettlementVO> getSettlement(@RequestParam Long projectId) {
        return R.ok(completionService.getSettlement(projectId));
    }

    @PostMapping("/settlement/generate")
    @PreAuthorize("hasAuthority('completion:create')")
    public R<Void> generateSettlement(@RequestParam Long projectId) {
        completionService.generateSettlement(projectId);
        return R.ok();
    }

    // ==================== LABOR SETTLEMENT ====================

    @GetMapping("/labor-settlements")
    @PreAuthorize("hasAuthority('completion:view')")
    public R<PageResult<LaborSettlementVO>> listLaborSettlements(CompletionQueryDTO query) {
        return R.ok(completionService.listLaborSettlements(query));
    }

    @PostMapping("/labor-settlements")
    @PreAuthorize("hasAuthority('completion:create')")
    public R<Long> createLaborSettlement(@Valid @RequestBody LaborSettlementCreateDTO dto) {
        return R.ok(completionService.createLaborSettlement(dto));
    }

    @PostMapping("/labor-settlements/{id}/approve")
    @PreAuthorize("hasAuthority('completion:approve')")
    public R<Void> approveLaborSettlement(@PathVariable Long id, @RequestParam String comment) {
        completionService.approveLaborSettlement(id, comment);
        return R.ok();
    }

    @PostMapping("/labor-settlements/{id}/reject")
    @PreAuthorize("hasAuthority('completion:approve')")
    public R<Void> rejectLaborSettlement(@PathVariable Long id, @RequestParam String comment) {
        completionService.rejectLaborSettlement(id, comment);
        return R.ok();
    }
}
