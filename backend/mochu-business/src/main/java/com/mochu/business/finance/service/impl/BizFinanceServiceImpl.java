package com.mochu.business.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.business.contract.entity.BizContract;
import com.mochu.business.contract.mapper.BizContractMapper;
import com.mochu.business.finance.dto.*;
import com.mochu.business.finance.entity.*;
import com.mochu.business.finance.mapper.*;
import com.mochu.business.finance.service.BizFinanceService;
import com.mochu.business.finance.vo.*;
import com.mochu.business.material.entity.BizMaterialOutbound;
import com.mochu.business.material.entity.BizMaterialOutboundItem;
import com.mochu.business.material.mapper.BizMaterialOutboundItemMapper;
import com.mochu.business.material.mapper.BizMaterialOutboundMapper;
import com.mochu.business.project.entity.BizProject;
import com.mochu.business.project.mapper.BizProjectMapper;
import com.mochu.business.supplier.entity.BizSupplier;
import com.mochu.business.supplier.mapper.BizSupplierMapper;
import com.mochu.common.constant.Constants;
import com.mochu.common.exception.BusinessException;
import com.mochu.common.result.PageResult;
import com.mochu.common.utils.BizNoGenerator;
import com.mochu.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizFinanceServiceImpl implements BizFinanceService {

    private final BizFinanceIncomeSplitMapper incomeSplitMapper;
    private final BizFinanceReconciliationMapper reconciliationMapper;
    private final BizFinancePaymentMapper paymentMapper;
    private final BizFinanceInvoiceMapper invoiceMapper;
    private final BizFinanceCostMapper costMapper;
    private final BizProjectMapper projectMapper;
    private final BizContractMapper contractMapper;
    private final BizSupplierMapper supplierMapper;
    private final BizMaterialOutboundMapper outboundMapper;
    private final BizMaterialOutboundItemMapper outboundItemMapper;
    private final BizNoGenerator bizNoGenerator;

    private static final Map<Integer, String> SPLIT_STATUS = Map.of(1, "草稿", 2, "待审批", 3, "已审批");
    private static final Map<Integer, String> RECON_STATUS = Map.of(1, "草稿", 2, "项目经理审", 3, "预算员审", 4, "财务审", 5, "总经理审", 6, "已确认");
    private static final Map<Integer, String> PAY_STATUS = Map.of(1, "草稿", 2, "预算员审", 3, "财务审", 4, "总经理审", 5, "已审批", 6, "已付款");
    private static final Map<Integer, String> COST_TYPE_MAP = Map.of(
            1, "人工费", 2, "材料费", 3, "机械费", 4, "管理费", 5, "措施费",
            6, "规费", 7, "税金", 8, "分包费", 9, "其他直接费", 10, "间接费"
    );

    // ==================== INCOME SPLIT ====================

    @Override
    @Transactional(readOnly = true)
    public PageResult<IncomeSplitVO> listIncomeSplits(FinanceQueryDTO query) {
        int page = query.getPage() != null ? query.getPage() : Constants.DEFAULT_PAGE;
        int size = Math.min(query.getSize() != null ? query.getSize() : Constants.DEFAULT_SIZE, Constants.MAX_SIZE);

        LambdaQueryWrapper<BizFinanceIncomeSplit> wrapper = new LambdaQueryWrapper<>();
        if (query.getProjectId() != null) wrapper.eq(BizFinanceIncomeSplit::getProjectId, query.getProjectId());
        if (query.getContractId() != null) wrapper.eq(BizFinanceIncomeSplit::getContractId, query.getContractId());
        if (query.getStatus() != null) wrapper.eq(BizFinanceIncomeSplit::getStatus, query.getStatus());
        wrapper.orderByDesc(BizFinanceIncomeSplit::getCreatedAt);

        IPage<BizFinanceIncomeSplit> pageResult = incomeSplitMapper.selectPage(new Page<>(page, size), wrapper);
        List<IncomeSplitVO> voList = pageResult.getRecords().stream().map(this::toSplitVO).collect(Collectors.toList());
        return new PageResult<>(voList, pageResult.getTotal(), page, size);
    }

    @Override
    @Transactional
    public Long createIncomeSplit(IncomeSplitCreateDTO dto) {
        // Validate contract total
        BizContract contract = contractMapper.selectById(dto.getContractId());
        if (contract == null) throw new BusinessException(404, "合同不存在");

        // Check split total ≤ contract amount (tolerance 0.01)
        LambdaQueryWrapper<BizFinanceIncomeSplit> sumWrapper = new LambdaQueryWrapper<>();
        sumWrapper.eq(BizFinanceIncomeSplit::getContractId, dto.getContractId())
                .ne(BizFinanceIncomeSplit::getStatus, 4); // exclude rejected
        List<BizFinanceIncomeSplit> existing = incomeSplitMapper.selectList(sumWrapper);
        BigDecimal existingTotal = existing.stream().map(BizFinanceIncomeSplit::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal newTotal = existingTotal.add(dto.getAmount());
        if (contract.getAmountWithTax() != null && newTotal.subtract(contract.getAmountWithTax()).compareTo(new BigDecimal("0.01")) > 0) {
            throw new BusinessException("收入拆分合计(" + newTotal + ")超过合同金额(" + contract.getAmountWithTax() + ")");
        }

        BizFinanceIncomeSplit split = new BizFinanceIncomeSplit();
        split.setProjectId(dto.getProjectId());
        split.setContractId(dto.getContractId());
        split.setSplitNo("IS" + System.currentTimeMillis() % 100000);
        split.setPeriod(dto.getPeriod());
        split.setAmount(dto.getAmount());
        split.setStatus(1);
        split.setCreatorId(SecurityUtils.getCurrentUserId());
        incomeSplitMapper.insert(split);
        return split.getId();
    }

    @Override
    @Transactional
    public void approveIncomeSplit(Long id, String comment) {
        BizFinanceIncomeSplit split = incomeSplitMapper.selectById(id);
        if (split == null) throw new BusinessException(404, "收入拆分不存在");
        if (comment == null || comment.length() < 2) throw new BusinessException("审批意见至少2个字符");
        if (split.getStatus() == 1 || split.getStatus() == 2) {
            split.setStatus(3);
            incomeSplitMapper.updateById(split);
        } else {
            throw new BusinessException("当前状态不支持审批");
        }
    }

    @Override
    @Transactional
    public void rejectIncomeSplit(Long id, String comment) {
        BizFinanceIncomeSplit split = incomeSplitMapper.selectById(id);
        if (split == null) throw new BusinessException(404, "收入拆分不存在");
        if (comment == null || comment.length() < 5) throw new BusinessException("驳回意见至少5个字符");
        split.setStatus(1);
        incomeSplitMapper.updateById(split);
    }

    // ==================== RECONCILIATION ====================

    @Override
    @Transactional(readOnly = true)
    public PageResult<ReconciliationVO> listReconciliations(FinanceQueryDTO query) {
        int page = query.getPage() != null ? query.getPage() : Constants.DEFAULT_PAGE;
        int size = Math.min(query.getSize() != null ? query.getSize() : Constants.DEFAULT_SIZE, Constants.MAX_SIZE);

        LambdaQueryWrapper<BizFinanceReconciliation> wrapper = new LambdaQueryWrapper<>();
        if (query.getProjectId() != null) wrapper.eq(BizFinanceReconciliation::getProjectId, query.getProjectId());
        if (query.getStatus() != null) wrapper.eq(BizFinanceReconciliation::getStatus, query.getStatus());
        if (StringUtils.hasText(query.getPeriod())) wrapper.eq(BizFinanceReconciliation::getPeriod, query.getPeriod());
        wrapper.orderByDesc(BizFinanceReconciliation::getCreatedAt);

        IPage<BizFinanceReconciliation> pageResult = reconciliationMapper.selectPage(new Page<>(page, size), wrapper);
        List<ReconciliationVO> voList = pageResult.getRecords().stream().map(this::toReconVO).collect(Collectors.toList());
        return new PageResult<>(voList, pageResult.getTotal(), page, size);
    }

    @Override
    @Transactional
    public void generateMonthlyReconciliation(String period) {
        // Get all active contracts with expenditure type
        LambdaQueryWrapper<BizContract> contractWrapper = new LambdaQueryWrapper<>();
        contractWrapper.eq(BizContract::getContractType, 2).in(BizContract::getStatus, 5, 6); // approved or executing
        List<BizContract> contracts = contractMapper.selectList(contractWrapper);

        for (BizContract contract : contracts) {
            // Check if already generated
            LambdaQueryWrapper<BizFinanceReconciliation> existWrapper = new LambdaQueryWrapper<>();
            existWrapper.eq(BizFinanceReconciliation::getContractId, contract.getId())
                    .eq(BizFinanceReconciliation::getPeriod, period);
            if (reconciliationMapper.selectCount(existWrapper) > 0) continue;

            BizFinanceReconciliation recon = new BizFinanceReconciliation();
            recon.setReconciliationNo(bizNoGenerator.generateReconciliationNo());
            recon.setProjectId(contract.getProjectId());
            recon.setSupplierId(contract.getSupplierId());
            recon.setContractId(contract.getId());
            recon.setPeriod(period);
            recon.setTotalAmount(BigDecimal.ZERO);
            recon.setConfirmedAmount(BigDecimal.ZERO);
            recon.setDifference(BigDecimal.ZERO);
            recon.setStatus(1);
            recon.setCreatorId(SecurityUtils.getCurrentUserId());
            reconciliationMapper.insert(recon);
        }
        log.info("Monthly reconciliation generated for period {}", period);
    }

    @Override
    @Transactional
    public void approveReconciliation(Long id, String comment) {
        BizFinanceReconciliation recon = reconciliationMapper.selectById(id);
        if (recon == null) throw new BusinessException(404, "对账单不存在");
        if (comment == null || comment.length() < 2) throw new BusinessException("审批意见至少2个字符");

        if (recon.getStatus() >= 1 && recon.getStatus() <= 5) {
            recon.setStatus(recon.getStatus() + 1);
            if (recon.getStatus() == 6) {
                recon.setConfirmedAt(LocalDateTime.now());
            }
            reconciliationMapper.updateById(recon);
        } else {
            throw new BusinessException("当前状态不支持审批");
        }
    }

    @Override
    @Transactional
    public void rejectReconciliation(Long id, String comment) {
        BizFinanceReconciliation recon = reconciliationMapper.selectById(id);
        if (recon == null) throw new BusinessException(404, "对账单不存在");
        if (comment == null || comment.length() < 5) throw new BusinessException("驳回意见至少5个字符");
        recon.setStatus(1);
        reconciliationMapper.updateById(recon);
    }

    // ==================== RECEIPT ====================

    @Override
    @Transactional
    public void registerReceipt(ReceiptDTO dto) {
        // Update contract cumulative received amount
        if (dto.getContractId() != null) {
            BizContract contract = contractMapper.selectById(dto.getContractId());
            if (contract != null) {
                BigDecimal received = contract.getReceivedAmount() != null ? contract.getReceivedAmount() : BigDecimal.ZERO;
                contract.setReceivedAmount(received.add(dto.getAmount()));
                contractMapper.updateById(contract);
            }
        }
        // Record as cost entry
        BizFinanceCost cost = new BizFinanceCost();
        cost.setProjectId(dto.getProjectId());
        cost.setCostType(0); // receipt type
        cost.setCategory("收款");
        cost.setAmount(dto.getAmount());
        cost.setDescription(dto.getRemark());
        cost.setPeriod(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")));
        costMapper.insert(cost);
        log.info("Receipt registered: {} for project {}", dto.getAmount(), dto.getProjectId());
    }

    // ==================== PAYMENT ====================

    @Override
    @Transactional(readOnly = true)
    public PageResult<PaymentVO> listPayments(FinanceQueryDTO query) {
        int page = query.getPage() != null ? query.getPage() : Constants.DEFAULT_PAGE;
        int size = Math.min(query.getSize() != null ? query.getSize() : Constants.DEFAULT_SIZE, Constants.MAX_SIZE);

        LambdaQueryWrapper<BizFinancePayment> wrapper = new LambdaQueryWrapper<>();
        if (query.getProjectId() != null) wrapper.eq(BizFinancePayment::getProjectId, query.getProjectId());
        if (query.getStatus() != null) wrapper.eq(BizFinancePayment::getStatus, query.getStatus());
        wrapper.orderByDesc(BizFinancePayment::getCreatedAt);

        IPage<BizFinancePayment> pageResult = paymentMapper.selectPage(new Page<>(page, size), wrapper);
        List<PaymentVO> voList = pageResult.getRecords().stream().map(this::toPaymentVO).collect(Collectors.toList());
        return new PageResult<>(voList, pageResult.getTotal(), page, size);
    }

    @Override
    @Transactional
    public Long createPayment(PaymentCreateDTO dto) {
        // Validate payment amount limits
        if (dto.getPaymentType() == 2 && dto.getReconciliationId() != null) {
            // Labor payment: cannot exceed reconciliation payable balance
            BizFinanceReconciliation recon = reconciliationMapper.selectById(dto.getReconciliationId());
            if (recon != null) {
                BigDecimal paid = getPaymentsPaidForReconciliation(dto.getReconciliationId());
                BigDecimal available = recon.getConfirmedAmount().subtract(paid);
                if (dto.getAmount().compareTo(available) > 0) {
                    throw new BusinessException("人工费付款金额(" + dto.getAmount() + ")超过对账单可付余额(" + available + ")");
                }
            }
        }
        if (dto.getPaymentType() == 1 && dto.getContractId() != null) {
            // Material payment: cannot exceed contract payable balance
            BizContract contract = contractMapper.selectById(dto.getContractId());
            if (contract != null) {
                BigDecimal paid = getPaymentsPaidForContract(dto.getContractId());
                BigDecimal available = contract.getAmountWithTax().subtract(paid);
                if (dto.getAmount().compareTo(available) > 0) {
                    throw new BusinessException("材料款付款金额(" + dto.getAmount() + ")超过合同可付余额(" + available + ")");
                }
            }
        }

        BizFinancePayment payment = new BizFinancePayment();
        payment.setPaymentNo(dto.getPaymentType() == 2
                ? bizNoGenerator.generateLaborPaymentNo()
                : bizNoGenerator.generateMaterialPaymentNo());
        payment.setPaymentType(dto.getPaymentType());
        payment.setProjectId(dto.getProjectId());
        payment.setContractId(dto.getContractId());
        payment.setSupplierId(dto.getSupplierId());
        payment.setReconciliationId(dto.getReconciliationId());
        payment.setAmount(dto.getAmount());
        payment.setPaidAmount(BigDecimal.ZERO);
        payment.setStatus(1);
        payment.setBankName(dto.getBankName());
        payment.setBankAccount(dto.getBankAccount());
        payment.setRemark(dto.getRemark());
        payment.setCreatorId(SecurityUtils.getCurrentUserId());
        paymentMapper.insert(payment);

        log.info("Payment created: {} type={}", payment.getPaymentNo(), dto.getPaymentType());
        return payment.getId();
    }

    @Override
    @Transactional
    public void approvePayment(Long id, String comment) {
        BizFinancePayment payment = paymentMapper.selectById(id);
        if (payment == null) throw new BusinessException(404, "付款单不存在");
        if (comment == null || comment.length() < 2) throw new BusinessException("审批意见至少2个字符");

        if (payment.getStatus() >= 1 && payment.getStatus() <= 4) {
            payment.setStatus(payment.getStatus() + 1);
            if (payment.getStatus() == 5) {
                payment.setApproverId(SecurityUtils.getCurrentUserId());
                payment.setApprovedAt(LocalDateTime.now());
            }
            paymentMapper.updateById(payment);
        } else {
            throw new BusinessException("当前状态不支持审批");
        }
    }

    @Override
    @Transactional
    public void rejectPayment(Long id, String comment) {
        BizFinancePayment payment = paymentMapper.selectById(id);
        if (payment == null) throw new BusinessException(404, "付款单不存在");
        if (comment == null || comment.length() < 5) throw new BusinessException("驳回意见至少5个字符");
        payment.setStatus(1);
        paymentMapper.updateById(payment);
    }

    @Override
    @Transactional
    public void markPaymentPaid(Long id) {
        BizFinancePayment payment = paymentMapper.selectById(id);
        if (payment == null) throw new BusinessException(404, "付款单不存在");
        if (payment.getStatus() != 5) throw new BusinessException("只有已审批的付款可以标记已付");
        payment.setStatus(6);
        payment.setPaidAmount(payment.getAmount());
        payment.setPaidAt(LocalDateTime.now());
        paymentMapper.updateById(payment);

        // Trigger cost aggregation
        aggregateCostForPayment(payment);
    }

    // ==================== INVOICE ====================

    @Override
    @Transactional(readOnly = true)
    public PageResult<InvoiceVO> listInvoices(FinanceQueryDTO query) {
        int page = query.getPage() != null ? query.getPage() : Constants.DEFAULT_PAGE;
        int size = Math.min(query.getSize() != null ? query.getSize() : Constants.DEFAULT_SIZE, Constants.MAX_SIZE);

        LambdaQueryWrapper<BizFinanceInvoice> wrapper = new LambdaQueryWrapper<>();
        if (query.getProjectId() != null) wrapper.eq(BizFinanceInvoice::getProjectId, query.getProjectId());
        if (query.getContractId() != null) wrapper.eq(BizFinanceInvoice::getContractId, query.getContractId());
        wrapper.orderByDesc(BizFinanceInvoice::getCreatedAt);

        IPage<BizFinanceInvoice> pageResult = invoiceMapper.selectPage(new Page<>(page, size), wrapper);
        List<InvoiceVO> voList = pageResult.getRecords().stream().map(this::toInvoiceVO).collect(Collectors.toList());
        return new PageResult<>(voList, pageResult.getTotal(), page, size);
    }

    @Override
    @Transactional
    public Long createInvoice(InvoiceCreateDTO dto) {
        BizFinanceInvoice invoice = new BizFinanceInvoice();
        invoice.setProjectId(dto.getProjectId());
        invoice.setContractId(dto.getContractId());
        invoice.setInvoiceNo(dto.getInvoiceNo());
        invoice.setInvoiceType(dto.getInvoiceType());
        invoice.setAmount(dto.getAmount());
        invoice.setTaxAmount(dto.getTaxAmount());
        invoice.setInvoiceDate(dto.getInvoiceDate());
        invoice.setFileUrl(dto.getFileUrl());
        invoice.setCreatorId(SecurityUtils.getCurrentUserId());
        invoiceMapper.insert(invoice);
        return invoice.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceVO> getExpiringInvoices(int days) {
        LocalDate deadline = LocalDate.now().minusDays(360 - days);
        LambdaQueryWrapper<BizFinanceInvoice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizFinanceInvoice::getInvoiceType, 1) // 专票
                .le(BizFinanceInvoice::getInvoiceDate, deadline);
        return invoiceMapper.selectList(wrapper).stream().map(this::toInvoiceVO).collect(Collectors.toList());
    }

    // ==================== COST ====================

    @Override
    @Transactional(readOnly = true)
    public List<CostVO> listCosts(Long projectId, String period) {
        LambdaQueryWrapper<BizFinanceCost> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) wrapper.eq(BizFinanceCost::getProjectId, projectId);
        if (StringUtils.hasText(period)) wrapper.eq(BizFinanceCost::getPeriod, period);
        wrapper.orderByAsc(BizFinanceCost::getCostType);
        return costMapper.selectList(wrapper).stream().map(this::toCostVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void aggregateCosts(Long projectId, String period) {
        // Aggregate material outbound costs
        LambdaQueryWrapper<BizMaterialOutbound> outWrapper = new LambdaQueryWrapper<>();
        outWrapper.eq(BizMaterialOutbound::getProjectId, projectId).eq(BizMaterialOutbound::getStatus, 5);
        List<BizMaterialOutbound> outbounds = outboundMapper.selectList(outWrapper);

        BigDecimal materialTotal = BigDecimal.ZERO;
        for (BizMaterialOutbound ob : outbounds) {
            LambdaQueryWrapper<BizMaterialOutboundItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(BizMaterialOutboundItem::getOutboundId, ob.getId());
            List<BizMaterialOutboundItem> items = outboundItemMapper.selectList(itemWrapper);
            materialTotal = materialTotal.add(items.stream()
                    .map(i -> i.getAmount() != null ? i.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }

        if (materialTotal.compareTo(BigDecimal.ZERO) > 0) {
            saveCostEntry(projectId, 2, "材料费", materialTotal, "材料出库自动归集", period);
        }

        // Aggregate labor payments
        LambdaQueryWrapper<BizFinancePayment> laborWrapper = new LambdaQueryWrapper<>();
        laborWrapper.eq(BizFinancePayment::getProjectId, projectId)
                .eq(BizFinancePayment::getPaymentType, 2)
                .eq(BizFinancePayment::getStatus, 6);
        List<BizFinancePayment> laborPayments = paymentMapper.selectList(laborWrapper);
        BigDecimal laborTotal = laborPayments.stream()
                .map(p -> p.getPaidAmount() != null ? p.getPaidAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (laborTotal.compareTo(BigDecimal.ZERO) > 0) {
            saveCostEntry(projectId, 1, "人工费", laborTotal, "人工费付款自动归集", period);
        }

        log.info("Cost aggregation completed for project {} period {}", projectId, period);
    }

    // ==================== HELPERS ====================

    private BigDecimal getPaymentsPaidForReconciliation(Long reconciliationId) {
        LambdaQueryWrapper<BizFinancePayment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizFinancePayment::getReconciliationId, reconciliationId)
                .ne(BizFinancePayment::getStatus, 1);
        return paymentMapper.selectList(wrapper).stream()
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getPaymentsPaidForContract(Long contractId) {
        LambdaQueryWrapper<BizFinancePayment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizFinancePayment::getContractId, contractId)
                .ne(BizFinancePayment::getStatus, 1);
        return paymentMapper.selectList(wrapper).stream()
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void aggregateCostForPayment(BizFinancePayment payment) {
        String period = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
        int costType = payment.getPaymentType() == 2 ? 1 : 2;
        String category = payment.getPaymentType() == 2 ? "人工费" : "材料费";
        saveCostEntry(payment.getProjectId(), costType, category, payment.getPaidAmount(),
                "付款 " + payment.getPaymentNo(), period);
    }

    private void saveCostEntry(Long projectId, int costType, String category, BigDecimal amount, String description, String period) {
        BizFinanceCost cost = new BizFinanceCost();
        cost.setProjectId(projectId);
        cost.setCostType(costType);
        cost.setCategory(category);
        cost.setAmount(amount);
        cost.setDescription(description);
        cost.setPeriod(period);
        costMapper.insert(cost);
    }

    // ==================== VO CONVERTERS ====================

    private IncomeSplitVO toSplitVO(BizFinanceIncomeSplit s) {
        IncomeSplitVO vo = new IncomeSplitVO();
        vo.setId(s.getId());
        vo.setProjectId(s.getProjectId());
        vo.setContractId(s.getContractId());
        vo.setSplitNo(s.getSplitNo());
        vo.setPeriod(s.getPeriod());
        vo.setAmount(s.getAmount());
        vo.setStatus(s.getStatus());
        vo.setStatusName(SPLIT_STATUS.getOrDefault(s.getStatus(), "未知"));
        vo.setCreatedAt(s.getCreatedAt());
        if (s.getProjectId() != null) {
            BizProject p = projectMapper.selectById(s.getProjectId());
            if (p != null) vo.setProjectName(p.getProjectName());
        }
        if (s.getContractId() != null) {
            BizContract c = contractMapper.selectById(s.getContractId());
            if (c != null) vo.setContractName(c.getContractName());
        }
        return vo;
    }

    private ReconciliationVO toReconVO(BizFinanceReconciliation r) {
        ReconciliationVO vo = new ReconciliationVO();
        vo.setId(r.getId());
        vo.setReconciliationNo(r.getReconciliationNo());
        vo.setProjectId(r.getProjectId());
        vo.setSupplierId(r.getSupplierId());
        vo.setContractId(r.getContractId());
        vo.setPeriod(r.getPeriod());
        vo.setTotalAmount(r.getTotalAmount());
        vo.setConfirmedAmount(r.getConfirmedAmount());
        vo.setDifference(r.getDifference());
        vo.setStatus(r.getStatus());
        vo.setStatusName(RECON_STATUS.getOrDefault(r.getStatus(), "未知"));
        vo.setConfirmedAt(r.getConfirmedAt());
        vo.setCreatedAt(r.getCreatedAt());
        if (r.getProjectId() != null) {
            BizProject p = projectMapper.selectById(r.getProjectId());
            if (p != null) vo.setProjectName(p.getProjectName());
        }
        if (r.getSupplierId() != null) {
            BizSupplier s = supplierMapper.selectById(r.getSupplierId());
            if (s != null) vo.setSupplierName(s.getSupplierName());
        }
        return vo;
    }

    private PaymentVO toPaymentVO(BizFinancePayment p) {
        PaymentVO vo = new PaymentVO();
        vo.setId(p.getId());
        vo.setPaymentNo(p.getPaymentNo());
        vo.setPaymentType(p.getPaymentType());
        vo.setPaymentTypeName(p.getPaymentType() == 1 ? "材料款" : "人工费");
        vo.setProjectId(p.getProjectId());
        vo.setContractId(p.getContractId());
        vo.setSupplierId(p.getSupplierId());
        vo.setAmount(p.getAmount());
        vo.setPaidAmount(p.getPaidAmount());
        vo.setStatus(p.getStatus());
        vo.setStatusName(PAY_STATUS.getOrDefault(p.getStatus(), "未知"));
        vo.setBankName(p.getBankName());
        vo.setBankAccount(p.getBankAccount());
        vo.setRemark(p.getRemark());
        vo.setApprovedAt(p.getApprovedAt());
        vo.setPaidAt(p.getPaidAt());
        vo.setCreatedAt(p.getCreatedAt());
        if (p.getProjectId() != null) {
            BizProject proj = projectMapper.selectById(p.getProjectId());
            if (proj != null) vo.setProjectName(proj.getProjectName());
        }
        if (p.getSupplierId() != null) {
            BizSupplier s = supplierMapper.selectById(p.getSupplierId());
            if (s != null) vo.setSupplierName(s.getSupplierName());
        }
        return vo;
    }

    private InvoiceVO toInvoiceVO(BizFinanceInvoice i) {
        InvoiceVO vo = new InvoiceVO();
        vo.setId(i.getId());
        vo.setProjectId(i.getProjectId());
        vo.setContractId(i.getContractId());
        vo.setInvoiceNo(i.getInvoiceNo());
        vo.setInvoiceType(i.getInvoiceType());
        vo.setInvoiceTypeName(i.getInvoiceType() == 1 ? "专票" : "普票");
        vo.setAmount(i.getAmount());
        vo.setTaxAmount(i.getTaxAmount());
        vo.setInvoiceDate(i.getInvoiceDate());
        vo.setFileUrl(i.getFileUrl());
        vo.setCreatedAt(i.getCreatedAt());
        if (i.getInvoiceDate() != null) {
            long daysSince = ChronoUnit.DAYS.between(i.getInvoiceDate(), LocalDate.now());
            vo.setDaysToExpire(360 - daysSince);
        }
        if (i.getProjectId() != null) {
            BizProject p = projectMapper.selectById(i.getProjectId());
            if (p != null) vo.setProjectName(p.getProjectName());
        }
        return vo;
    }

    private CostVO toCostVO(BizFinanceCost c) {
        CostVO vo = new CostVO();
        vo.setId(c.getId());
        vo.setProjectId(c.getProjectId());
        vo.setCostType(c.getCostType());
        vo.setCostTypeName(COST_TYPE_MAP.getOrDefault(c.getCostType(), c.getCategory()));
        vo.setCategory(c.getCategory());
        vo.setAmount(c.getAmount());
        vo.setDescription(c.getDescription());
        vo.setPeriod(c.getPeriod());
        if (c.getProjectId() != null) {
            BizProject p = projectMapper.selectById(c.getProjectId());
            if (p != null) vo.setProjectName(p.getProjectName());
        }
        return vo;
    }
}
