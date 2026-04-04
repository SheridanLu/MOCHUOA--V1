package com.mochu.business.contract.controller;

import com.mochu.business.contract.dto.ContractCreateDTO;
import com.mochu.business.contract.dto.ContractQueryDTO;
import com.mochu.business.contract.dto.SupplementCreateDTO;
import com.mochu.business.contract.service.BizContractService;
import com.mochu.business.contract.vo.ContractVO;
import com.mochu.common.result.PageResult;
import com.mochu.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
public class BizContractController {

    private final BizContractService contractService;

    @GetMapping
    @PreAuthorize("hasAuthority('contract:view')")
    public R<PageResult<ContractVO>> list(ContractQueryDTO query) {
        return R.ok(contractService.listContracts(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('contract:view')")
    public R<ContractVO> getById(@PathVariable Long id) {
        return R.ok(contractService.getContractById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('contract:create')")
    public R<Long> create(@Valid @RequestBody ContractCreateDTO dto) {
        return R.ok(contractService.createContract(dto));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('contract:create')")
    public R<Void> submit(@PathVariable Long id) {
        contractService.submitForApproval(id);
        return R.ok();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('contract:approve')")
    public R<Void> approve(@PathVariable Long id, @RequestParam String comment) {
        contractService.approve(id, comment);
        return R.ok();
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('contract:approve')")
    public R<Void> reject(@PathVariable Long id, @RequestParam String comment) {
        contractService.reject(id, comment);
        return R.ok();
    }

    @PostMapping("/{id}/terminate")
    @PreAuthorize("hasAuthority('contract:terminate')")
    public R<Void> terminate(@PathVariable Long id, @RequestParam String reason) {
        contractService.terminate(id, reason);
        return R.ok();
    }

    @PostMapping("/{contractId}/supplements")
    @PreAuthorize("hasAuthority('contract:create')")
    public R<Long> createSupplement(@PathVariable Long contractId, @Valid @RequestBody SupplementCreateDTO dto) {
        return R.ok(contractService.createSupplement(contractId, dto));
    }
}
