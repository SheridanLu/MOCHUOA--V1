package com.mochu.business.project.controller;

import com.mochu.business.project.dto.ProjectCreateDTO;
import com.mochu.business.project.dto.ProjectQueryDTO;
import com.mochu.business.project.dto.ProjectUpdateDTO;
import com.mochu.business.project.service.BizProjectService;
import com.mochu.business.project.vo.ProjectVO;
import com.mochu.common.result.PageResult;
import com.mochu.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class BizProjectController {

    private final BizProjectService projectService;

    @GetMapping
    @PreAuthorize("hasAuthority('project:view')")
    public R<PageResult<ProjectVO>> list(ProjectQueryDTO query) {
        return R.ok(projectService.listProjects(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('project:view')")
    public R<ProjectVO> getById(@PathVariable Long id) {
        return R.ok(projectService.getProjectById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('project:create')")
    public R<Long> create(@Valid @RequestBody ProjectCreateDTO dto) {
        return R.ok(projectService.createProject(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('project:edit')")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ProjectUpdateDTO dto) {
        projectService.updateProject(id, dto);
        return R.ok();
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('project:create')")
    public R<Void> submit(@PathVariable Long id) {
        projectService.submitForApproval(id);
        return R.ok();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('project:approve')")
    public R<Void> approve(@PathVariable Long id, @RequestParam String comment) {
        projectService.approve(id, comment);
        return R.ok();
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('project:approve')")
    public R<Void> reject(@PathVariable Long id, @RequestParam String comment) {
        projectService.reject(id, comment);
        return R.ok();
    }

    @PostMapping("/{id}/pause")
    @PreAuthorize("hasAuthority('project:edit')")
    public R<Void> pause(@PathVariable Long id) {
        projectService.pause(id);
        return R.ok();
    }

    @PostMapping("/{id}/resume")
    @PreAuthorize("hasAuthority('project:edit')")
    public R<Void> resume(@PathVariable Long id) {
        projectService.resume(id);
        return R.ok();
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('project:edit')")
    public R<Void> close(@PathVariable Long id) {
        projectService.close(id);
        return R.ok();
    }

    @PostMapping("/{id}/terminate")
    @PreAuthorize("hasAuthority('project:edit')")
    public R<Void> terminate(@PathVariable Long id, @RequestParam String reason) {
        projectService.terminate(id, reason);
        return R.ok();
    }

    @PostMapping("/{id}/convert")
    @PreAuthorize("hasAuthority('project:create')")
    public R<Void> convert(@PathVariable Long id, @RequestParam(required = false) Long costTargetProjectId) {
        projectService.convertVirtualToEntity(id, costTargetProjectId);
        return R.ok();
    }
}
