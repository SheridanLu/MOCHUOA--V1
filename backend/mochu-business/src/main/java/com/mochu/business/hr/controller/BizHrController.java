package com.mochu.business.hr.controller;

import com.mochu.business.hr.dto.*;
import com.mochu.business.hr.service.BizHrService;
import com.mochu.business.hr.vo.*;
import com.mochu.common.result.PageResult;
import com.mochu.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hr")
@RequiredArgsConstructor
public class BizHrController {

    private final BizHrService hrService;

    // ==================== EMPLOYEE ====================

    @GetMapping("/employees")
    @PreAuthorize("hasAuthority('hr:view')")
    public R<PageResult<EmployeeVO>> listEmployees(EmployeeQueryDTO query) {
        return R.ok(hrService.listEmployees(query));
    }

    @GetMapping("/employees/{id}")
    @PreAuthorize("hasAuthority('hr:view')")
    public R<EmployeeVO> getEmployee(@PathVariable Long id) {
        return R.ok(hrService.getEmployeeById(id));
    }

    @PostMapping("/employees/onboard")
    @PreAuthorize("hasAuthority('hr:create')")
    public R<Long> onboard(@Valid @RequestBody EmployeeCreateDTO dto) {
        return R.ok(hrService.onboard(dto));
    }

    @PostMapping("/employees/{id}/offboard")
    @PreAuthorize("hasAuthority('hr:create')")
    public R<Void> offboard(@PathVariable Long id) {
        hrService.offboard(id);
        return R.ok();
    }

    // ==================== PAYROLL ====================

    @GetMapping("/payrolls")
    @PreAuthorize("hasAuthority('hr:view')")
    public R<PageResult<PayrollVO>> listPayrolls(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return R.ok(hrService.listPayrolls(period, status, page, size));
    }

    @PostMapping("/payrolls/generate")
    @PreAuthorize("hasAuthority('hr:create')")
    public R<Void> generatePayroll(@RequestParam String period) {
        hrService.generateMonthlyPayroll(period);
        return R.ok();
    }

    @PutMapping("/payrolls/{id}/adjust")
    @PreAuthorize("hasAuthority('finance:approve')")
    public R<Void> adjustPayroll(@PathVariable Long id, @RequestBody PayrollAdjustDTO dto) {
        hrService.adjustPayroll(id, dto);
        return R.ok();
    }

    @PostMapping("/payrolls/{id}/approve")
    @PreAuthorize("hasAuthority('finance:approve')")
    public R<Void> approvePayroll(@PathVariable Long id, @RequestParam String comment) {
        hrService.approvePayroll(id, comment);
        return R.ok();
    }

    @PostMapping("/payrolls/{id}/paid")
    @PreAuthorize("hasAuthority('finance:approve')")
    public R<Void> markPaid(@PathVariable Long id) {
        hrService.markPayrollPaid(id);
        return R.ok();
    }

    // ==================== REIMBURSEMENT ====================

    @GetMapping("/reimbursements")
    @PreAuthorize("hasAuthority('hr:view')")
    public R<PageResult<ReimbursementVO>> listReimbursements(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return R.ok(hrService.listReimbursements(employeeId, status, page, size));
    }

    @PostMapping("/reimbursements")
    @PreAuthorize("hasAuthority('hr:create')")
    public R<Long> createReimbursement(@Valid @RequestBody ReimbursementCreateDTO dto) {
        return R.ok(hrService.createReimbursement(dto));
    }

    @PostMapping("/reimbursements/{id}/approve")
    @PreAuthorize("hasAuthority('hr:approve')")
    public R<Void> approveReimbursement(@PathVariable Long id, @RequestParam String comment) {
        hrService.approveReimbursement(id, comment);
        return R.ok();
    }

    @PostMapping("/reimbursements/{id}/reject")
    @PreAuthorize("hasAuthority('hr:approve')")
    public R<Void> rejectReimbursement(@PathVariable Long id, @RequestParam String comment) {
        hrService.rejectReimbursement(id, comment);
        return R.ok();
    }

    // ==================== CONTRACT ====================

    @GetMapping("/contracts")
    @PreAuthorize("hasAuthority('hr:view')")
    public R<List<HrContractVO>> listContracts(@RequestParam(required = false) Long employeeId) {
        return R.ok(hrService.listContracts(employeeId));
    }

    @PostMapping("/contracts")
    @PreAuthorize("hasAuthority('hr:create')")
    public R<Long> createContract(@Valid @RequestBody HrContractCreateDTO dto) {
        return R.ok(hrService.createContract(dto));
    }

    @PostMapping("/contracts/{id}/renew")
    @PreAuthorize("hasAuthority('hr:create')")
    public R<Void> renewContract(@PathVariable Long id, @Valid @RequestBody HrContractCreateDTO dto) {
        hrService.renewContract(id, dto);
        return R.ok();
    }

    @GetMapping("/contracts/expiring")
    @PreAuthorize("hasAuthority('hr:view')")
    public R<List<HrContractVO>> expiringContracts(@RequestParam(defaultValue = "30") int days) {
        return R.ok(hrService.getExpiringContracts(days));
    }

    // ==================== QUALIFICATION ====================

    @GetMapping("/qualifications")
    @PreAuthorize("hasAuthority('hr:view')")
    public R<List<QualificationVO>> listQualifications(@RequestParam(required = false) Long employeeId) {
        return R.ok(hrService.listQualifications(employeeId));
    }

    @PostMapping("/qualifications")
    @PreAuthorize("hasAuthority('hr:create')")
    public R<Long> createQualification(
            @RequestParam Long employeeId,
            @RequestParam String qualName,
            @RequestParam(required = false) String qualNo,
            @RequestParam(required = false) String issueDate,
            @RequestParam(required = false) String expireDate,
            @RequestParam(required = false) String fileUrl) {
        return R.ok(hrService.createQualification(employeeId, qualName, qualNo, issueDate, expireDate, fileUrl));
    }

    @GetMapping("/qualifications/expiring")
    @PreAuthorize("hasAuthority('hr:view')")
    public R<List<QualificationVO>> expiringQualifications(@RequestParam(defaultValue = "30") int days) {
        return R.ok(hrService.getExpiringQualifications(days));
    }
}
