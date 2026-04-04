package com.mochu.business.finance.service;

import com.mochu.business.finance.dto.*;
import com.mochu.business.finance.vo.*;
import com.mochu.common.result.PageResult;

import java.util.List;

public interface BizFinanceService {
    // Income Split
    PageResult<IncomeSplitVO> listIncomeSplits(FinanceQueryDTO query);
    Long createIncomeSplit(IncomeSplitCreateDTO dto);
    void approveIncomeSplit(Long id, String comment);
    void rejectIncomeSplit(Long id, String comment);

    // Reconciliation
    PageResult<ReconciliationVO> listReconciliations(FinanceQueryDTO query);
    void generateMonthlyReconciliation(String period);
    void approveReconciliation(Long id, String comment);
    void rejectReconciliation(Long id, String comment);

    // Receipt (no approval)
    void registerReceipt(ReceiptDTO dto);

    // Payment
    PageResult<PaymentVO> listPayments(FinanceQueryDTO query);
    Long createPayment(PaymentCreateDTO dto);
    void approvePayment(Long id, String comment);
    void rejectPayment(Long id, String comment);
    void markPaymentPaid(Long id);

    // Invoice
    PageResult<InvoiceVO> listInvoices(FinanceQueryDTO query);
    Long createInvoice(InvoiceCreateDTO dto);
    List<InvoiceVO> getExpiringInvoices(int days);

    // Cost
    List<CostVO> listCosts(Long projectId, String period);
    void aggregateCosts(Long projectId, String period);
}
