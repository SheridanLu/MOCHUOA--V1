package com.mochu.business.showcase.controller;

import com.mochu.business.showcase.dto.ShowcaseCreateDTO;
import com.mochu.business.showcase.dto.ShowcaseQueryDTO;
import com.mochu.business.showcase.service.BizShowcaseService;
import com.mochu.business.showcase.vo.ShowcaseVO;
import com.mochu.common.result.PageResult;
import com.mochu.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/showcases")
@RequiredArgsConstructor
public class BizShowcaseController {

    private final BizShowcaseService showcaseService;

    @GetMapping
    @PreAuthorize("hasAuthority('showcase:view')")
    public R<PageResult<ShowcaseVO>> listShowcases(ShowcaseQueryDTO query) {
        return R.ok(showcaseService.listShowcases(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('showcase:view')")
    public R<ShowcaseVO> getShowcaseDetail(@PathVariable Long id) {
        return R.ok(showcaseService.getShowcaseDetail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('showcase:create')")
    public R<Long> createShowcase(@Valid @RequestBody ShowcaseCreateDTO dto) {
        return R.ok(showcaseService.createShowcase(dto));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('showcase:create')")
    public R<Void> submitForApproval(@PathVariable Long id) {
        showcaseService.submitForApproval(id);
        return R.ok();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('showcase:approve')")
    public R<Void> approveShowcase(@PathVariable Long id, @RequestParam String comment) {
        showcaseService.approveShowcase(id, comment);
        return R.ok();
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('showcase:approve')")
    public R<Void> rejectShowcase(@PathVariable Long id, @RequestParam String comment) {
        showcaseService.rejectShowcase(id, comment);
        return R.ok();
    }

    @PatchMapping("/{id}/visibility")
    @PreAuthorize("hasAuthority('showcase:create')")
    public R<Void> setVisibility(@PathVariable Long id, @RequestParam Integer visibility) {
        showcaseService.setVisibility(id, visibility);
        return R.ok();
    }
}
