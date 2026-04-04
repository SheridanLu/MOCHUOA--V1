package com.mochu.business.progress.controller;

import com.mochu.business.progress.dto.PlanCreateDTO;
import com.mochu.business.progress.dto.ProgressUpdateDTO;
import com.mochu.business.progress.dto.TaskCreateDTO;
import com.mochu.business.progress.service.BizProgressService;
import com.mochu.business.progress.vo.DeviationVO;
import com.mochu.business.progress.vo.PlanVO;
import com.mochu.business.progress.vo.TaskVO;
import com.mochu.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
public class BizProgressController {

    private final BizProgressService progressService;

    @GetMapping("/plans")
    @PreAuthorize("hasAuthority('progress:view')")
    public R<List<PlanVO>> listPlans(@RequestParam(required = false) Long projectId) {
        return R.ok(progressService.listPlans(projectId));
    }

    @PostMapping("/plans")
    @PreAuthorize("hasAuthority('progress:create')")
    public R<Long> createPlan(@Valid @RequestBody PlanCreateDTO dto) {
        return R.ok(progressService.createPlan(dto));
    }

    @PostMapping("/plans/{id}/approve")
    @PreAuthorize("hasAuthority('progress:approve')")
    public R<Void> approvePlan(@PathVariable Long id, @RequestParam String comment) {
        progressService.approvePlan(id, comment);
        return R.ok();
    }

    @GetMapping("/tasks")
    @PreAuthorize("hasAuthority('progress:view')")
    public R<List<TaskVO>> getGanttTasks(@RequestParam Long planId) {
        return R.ok(progressService.getGanttTasks(planId));
    }

    @PostMapping("/tasks")
    @PreAuthorize("hasAuthority('progress:create')")
    public R<Long> createTask(@Valid @RequestBody TaskCreateDTO dto) {
        return R.ok(progressService.createTask(dto));
    }

    @PostMapping("/progress/update")
    @PreAuthorize("hasAuthority('progress:create')")
    public R<Void> updateProgress(@Valid @RequestBody ProgressUpdateDTO dto) {
        progressService.updateProgress(dto);
        return R.ok();
    }

    @PostMapping("/progress/batch-update")
    @PreAuthorize("hasAuthority('progress:create')")
    public R<Void> batchUpdateProgress(@Valid @RequestBody ProgressUpdateDTO dto) {
        progressService.batchUpdateProgress(dto);
        return R.ok();
    }

    @GetMapping("/deviations")
    @PreAuthorize("hasAuthority('progress:view')")
    public R<List<DeviationVO>> listDeviations(@RequestParam(required = false) Long projectId) {
        return R.ok(progressService.listDeviations(projectId));
    }

    @PostMapping("/deviations/scan")
    @PreAuthorize("hasAuthority('progress:approve')")
    public R<Void> scanDeviations(@RequestParam Long projectId) {
        progressService.scanDeviations(projectId);
        return R.ok();
    }
}
