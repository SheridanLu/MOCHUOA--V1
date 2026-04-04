package com.mochu.business.contract.controller;

import com.mochu.business.contract.dto.TemplateCreateDTO;
import com.mochu.business.contract.service.BizContractTemplateService;
import com.mochu.business.contract.vo.TemplateVO;
import com.mochu.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contract-templates")
@RequiredArgsConstructor
public class BizContractTemplateController {

    private final BizContractTemplateService templateService;

    @GetMapping
    @PreAuthorize("hasAuthority('contract:view')")
    public R<List<TemplateVO>> list(@RequestParam(required = false) Integer templateType,
                                    @RequestParam(required = false) Integer status) {
        return R.ok(templateService.listTemplates(templateType, status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('contract:view')")
    public R<TemplateVO> getById(@PathVariable Long id) {
        return R.ok(templateService.getTemplateById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('template:create')")
    public R<Long> create(@Valid @RequestBody TemplateCreateDTO dto) {
        return R.ok(templateService.createTemplate(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('template:edit')")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody TemplateCreateDTO dto) {
        templateService.updateTemplate(id, dto);
        return R.ok();
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('template:create')")
    public R<Void> submit(@PathVariable Long id) {
        templateService.submitForApproval(id);
        return R.ok();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('template:approve')")
    public R<Void> approve(@PathVariable Long id, @RequestParam String comment) {
        templateService.approve(id, comment);
        return R.ok();
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('template:approve')")
    public R<Void> reject(@PathVariable Long id, @RequestParam String comment) {
        templateService.reject(id, comment);
        return R.ok();
    }

    @GetMapping("/{id}/render")
    @PreAuthorize("hasAuthority('contract:view')")
    public R<String> render(@PathVariable Long id) {
        return R.ok(templateService.renderTemplate(id));
    }
}
