package com.mochu.business.finance.controller;

import com.mochu.business.finance.dto.*;
import com.mochu.business.finance.service.BizFinanceService;
import com.mochu.business.finance.vo.*;
import com.mochu.common.result.PageResult;
import com.mochu.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance")
@RequiredArgsConstructor
public class BizFinanceController {

    private final BizFinanceService financeService;

    // ==================== INCOME SPLIT ====================

    @GetMapping("/income-splits")
    @PreAuthorize("hasAuthority('finance:view')")
    public R<PageResult<IncomeSplitVO>> listIncomeSplits(FinanceQueryDTO query) {
        return R.ok(financeService.listIncomeSplits(query));
    }

    @PostMapping("/income-splits")
    @PreAuthorize("hasAuthority('finance:create')")
    public R<Long> createIncomeSplit(@Valid @RequestBody IncomeSplitCreateDTO dto) {
        return R.ok(financeService.createIncomeSplit(dto));
    }

    @PostMapping("/income-splits/{id}/approve")
    @PreAuthorize("hasAuthority('finance:approve')")
    public R<Void> approveIncomeSplit(@PathVariable Long id, @RequestParam String comment) {
        financeService.approveIncomeSplit(id, comment);
        return R.ok();
    }

    @PostMapping("/income-splits/{id}/reject")
    @PreAuthorize("hasAuthority('finance:approve')")
    public R<Void> rejectIncomeSplit(@PathVariable Long id, @RequestParam String comment) {
        financeService.rejectIncomeSplit(id, comment);
        return R.ok();
    }

    // ==================== RECONCILIATION ====================

    @GetMapping("/reconciliations")
    @PreAuthorize("hasAuthority('finance:view')")
    public R<PageResult<ReconciliationVO>> listReconciliations(FinanceQueryDTO query) {
        return R.ok(financeService.listReconciliations(query));
    }

    @PostMapping("/reconciliations/generate")
    @PreAuthorize("hasAuthority('finance:create')")
    public R<Void> generateMonthlyReconciliation(@RequestParam String period) {
        financeService.generateMonthlyReconciliation(period);
        return R.ok();
    }

    @PostMapping("/reconciliations/{id}/approve")
    @PreAuthorize("hasAuthority('finance:approve')")
    public R<Void> approveReconciliation(@PathVariable Long id, @RequestParam String comment) {
        financeService.approveReconciliation(id, comment);
        return R.ok();
    }

    @PostMapping("/reconciliations/{id}/reject")
    @PreAuthorize("hasAuthority('finance:approve')")
    public R<Void> rejectReconciliation(@PathVariable Long id, @RequestParam String comment) {
        financeService.rejectReconciliation(id, comment);
        return R.ok();
    }

    // ==================== RECEIPT ====================

    @PostMapping("/receipts")
    @PreAuthorize("hasAuthority('finance:create')")
    public R<Void> registerReceipt(@Valid @RequestBody ReceiptDTO dto) {
        financeService.registerReceipt(dto);
        return R.ok();
    }

    // ==================== PAYMENT ====================

    @GetMapping("/payments")
    @PreAuthorize("hasAuthority('finance:view')")
    public R<PageResult<PaymentVO>> listPayments(FinanceQueryDTO query) {
        return R.ok(financeService.listPayments(query));
    }

    @PostMapping("/payments")
    @PreAuthorize("hasAuthority('finance:create')")
    public R<Long> createPayment(@Valid @RequestBody PaymentCreateDTO dto) {
        return R.ok(financeService.createPayment(dto));
    }

    @PostMapping("/payments/{id}/approve")
    @PreAuthorize("hasAuthority('finance:approve')")
    public R<Void> approvePayment(@PathVariable Long id, @RequestParam String comment) {
        financeService.approvePayment(id, comment);
        return R.ok();
    }

    @PostMapping("/payments/{id}/reject")
    @PreAuthorize("hasAuthority('finance:approve')")
    public R<Void> rejectPayment(@PathVariable Long id, @RequestParam String comment) {
        financeService.rejectPayment(id, comment);
        return R.ok();
    }

    @PostMapping("/payments/{id}/paid")
    @PreAuthorize("hasAuthority('finance:approve')")
    public R<Void> markPaymentPaid(@PathVariable Long id) {
        financeService.markPaymentPaid(id);
        return R.ok();
    }

    // ==================== INVOICE ====================

    @GetMapping("/invoices")
    @PreAuthorize("hasAuthority('finance:view')")
    public R<PageResult<InvoiceVO>> listInvoices(FinanceQueryDTO query) {
        return R.ok(financeService.listInvoices(query));
    }

    @PostMapping("/invoices")
    @PreAuthorize("hasAuthority('finance:create')")
    public R<Long> createInvoice(@Valid @RequestBody InvoiceCreateDTO dto) {
        return R.ok(financeService.createInvoice(dto));
    }

    @GetMapping("/invoices/expiring")
    @PreAuthorize("hasAuthority('finance:view')")
    public R<List<InvoiceVO>> getExpiringInvoices(@RequestParam(defaultValue = "30") int days) {
        return R.ok(financeService.getExpiringInvoices(days));
    }

    // ==================== COST ====================

    @GetMapping("/costs")
    @PreAuthorize("hasAuthority('finance:view')")
    public R<List<CostVO>> listCosts(@RequestParam Long projectId, @RequestParam(required = false) String period) {
        return R.ok(financeService.listCosts(projectId, period));
    }

    @PostMapping("/costs/aggregate")
    @PreAuthorize("hasAuthority('finance:create')")
    public R<Void> aggregateCosts(@RequestParam Long projectId, @RequestParam String period) {
        financeService.aggregateCosts(projectId, period);
        return R.ok();
    }
}
