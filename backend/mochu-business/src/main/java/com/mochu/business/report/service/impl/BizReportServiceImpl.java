package com.mochu.business.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mochu.business.finance.entity.BizFinanceCost;
import com.mochu.business.finance.entity.BizFinanceIncomeSplit;
import com.mochu.business.finance.entity.BizFinancePayment;
import com.mochu.business.finance.mapper.BizFinanceCostMapper;
import com.mochu.business.finance.mapper.BizFinanceIncomeSplitMapper;
import com.mochu.business.finance.mapper.BizFinancePaymentMapper;
import com.mochu.business.project.entity.BizProject;
import com.mochu.business.project.mapper.BizProjectMapper;
import com.mochu.business.report.dto.ReportQueryDTO;
import com.mochu.business.report.entity.BizReportSnapshot;
import com.mochu.business.report.mapper.BizReportSnapshotMapper;
import com.mochu.business.report.service.BizReportService;
import com.mochu.business.report.vo.CostSummaryVO;
import com.mochu.business.report.vo.IncomeExpenseVO;
import com.mochu.business.report.vo.ReportVO;
import com.mochu.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizReportServiceImpl implements BizReportService {

    private final BizReportSnapshotMapper reportSnapshotMapper;
    private final BizFinanceCostMapper costMapper;
    private final BizFinanceIncomeSplitMapper incomeSplitMapper;
    private final BizFinancePaymentMapper paymentMapper;
    private final BizProjectMapper projectMapper;
    private final ObjectMapper objectMapper;

    private static final String REPORT_TYPE_COST = "project_cost";
    private static final String REPORT_TYPE_INCOME_EXPENSE = "income_expense";
    private static final String REPORT_TYPE_PROCUREMENT = "procurement";
    private static final String REPORT_TYPE_INVENTORY = "inventory";

    // ==================== PROJECT COST REPORT ====================

    @Override
    @Transactional(readOnly = true)
    public ReportVO getProjectCostReport(ReportQueryDTO query) {
        LambdaQueryWrapper<BizFinanceCost> wrapper = new LambdaQueryWrapper<>();
        if (query.getProjectId() != null) {
            wrapper.eq(BizFinanceCost::getProjectId, query.getProjectId());
        }
        if (StringUtils.hasText(query.getPeriod())) {
            wrapper.eq(BizFinanceCost::getPeriod, query.getPeriod());
        }
        wrapper.orderByAsc(BizFinanceCost::getProjectId);

        List<BizFinanceCost> costs = costMapper.selectList(wrapper);

        // Group by projectId and aggregate by category
        Map<Long, List<BizFinanceCost>> grouped = costs.stream()
                .filter(c -> c.getProjectId() != null)
                .collect(Collectors.groupingBy(BizFinanceCost::getProjectId));

        List<CostSummaryVO> summaries = new ArrayList<>();
        for (Map.Entry<Long, List<BizFinanceCost>> entry : grouped.entrySet()) {
            Long projectId = entry.getKey();
            List<BizFinanceCost> projectCosts = entry.getValue();

            CostSummaryVO vo = new CostSummaryVO();
            vo.setProjectId(projectId);
            vo.setPeriod(StringUtils.hasText(query.getPeriod()) ? query.getPeriod() : "all");

            // Lookup project name
            BizProject project = projectMapper.selectById(projectId);
            if (project != null) {
                vo.setProjectName(project.getProjectName());
            }

            // Aggregate by category (costType: 1=labor, 2=material, 3=equipment, 4=management)
            BigDecimal materialCost = BigDecimal.ZERO;
            BigDecimal laborCost = BigDecimal.ZERO;
            BigDecimal equipmentCost = BigDecimal.ZERO;
            BigDecimal managementCost = BigDecimal.ZERO;
            BigDecimal otherCost = BigDecimal.ZERO;

            for (BizFinanceCost cost : projectCosts) {
                BigDecimal amount = cost.getAmount() != null ? cost.getAmount() : BigDecimal.ZERO;
                if (cost.getCostType() == null) {
                    otherCost = otherCost.add(amount);
                    continue;
                }
                switch (cost.getCostType()) {
                    case 1 -> laborCost = laborCost.add(amount);
                    case 2 -> materialCost = materialCost.add(amount);
                    case 3 -> equipmentCost = equipmentCost.add(amount);
                    case 4 -> managementCost = managementCost.add(amount);
                    default -> otherCost = otherCost.add(amount);
                }
            }

            vo.setMaterialCost(materialCost);
            vo.setLaborCost(laborCost);
            vo.setEquipmentCost(equipmentCost);
            vo.setManagementCost(managementCost);
            vo.setOtherCost(otherCost);
            vo.setTotalCost(materialCost.add(laborCost).add(equipmentCost).add(managementCost).add(otherCost));

            summaries.add(vo);
        }

        ReportVO report = new ReportVO();
        report.setReportType(REPORT_TYPE_COST);
        report.setReportName("项目成本汇总报表");
        report.setPeriod(StringUtils.hasText(query.getPeriod()) ? query.getPeriod() : "all");
        report.setGeneratedAt(LocalDateTime.now());
        report.setData(summaries);
        return report;
    }

    // ==================== INCOME VS EXPENSE REPORT ====================

    @Override
    @Transactional(readOnly = true)
    public ReportVO getIncomeExpenseReport(ReportQueryDTO query) {
        // Query approved income splits (status = 3)
        LambdaQueryWrapper<BizFinanceIncomeSplit> incomeWrapper = new LambdaQueryWrapper<>();
        incomeWrapper.eq(BizFinanceIncomeSplit::getStatus, 3);
        if (query.getProjectId() != null) {
            incomeWrapper.eq(BizFinanceIncomeSplit::getProjectId, query.getProjectId());
        }
        if (StringUtils.hasText(query.getPeriod())) {
            incomeWrapper.eq(BizFinanceIncomeSplit::getPeriod, query.getPeriod());
        }
        List<BizFinanceIncomeSplit> incomes = incomeSplitMapper.selectList(incomeWrapper);

        // Query paid payments (status = 6)
        LambdaQueryWrapper<BizFinancePayment> expenseWrapper = new LambdaQueryWrapper<>();
        expenseWrapper.eq(BizFinancePayment::getStatus, 6);
        if (query.getProjectId() != null) {
            expenseWrapper.eq(BizFinancePayment::getProjectId, query.getProjectId());
        }
        List<BizFinancePayment> expenses = paymentMapper.selectList(expenseWrapper);

        // Group income by project
        Map<Long, BigDecimal> incomeByProject = incomes.stream()
                .filter(i -> i.getProjectId() != null)
                .collect(Collectors.groupingBy(
                        BizFinanceIncomeSplit::getProjectId,
                        Collectors.reducing(BigDecimal.ZERO,
                                i -> i.getAmount() != null ? i.getAmount() : BigDecimal.ZERO,
                                BigDecimal::add)));

        // Group expense by project
        Map<Long, BigDecimal> expenseByProject = expenses.stream()
                .filter(e -> e.getProjectId() != null)
                .collect(Collectors.groupingBy(
                        BizFinancePayment::getProjectId,
                        Collectors.reducing(BigDecimal.ZERO,
                                e -> e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO,
                                BigDecimal::add)));

        // Merge all project IDs
        Set<Long> allProjectIds = new HashSet<>();
        allProjectIds.addAll(incomeByProject.keySet());
        allProjectIds.addAll(expenseByProject.keySet());

        List<IncomeExpenseVO> results = new ArrayList<>();
        for (Long projectId : allProjectIds) {
            IncomeExpenseVO vo = new IncomeExpenseVO();
            vo.setProjectId(projectId);
            vo.setPeriod(StringUtils.hasText(query.getPeriod()) ? query.getPeriod() : "all");

            BizProject project = projectMapper.selectById(projectId);
            if (project != null) {
                vo.setProjectName(project.getProjectName());
            }

            BigDecimal totalIncome = incomeByProject.getOrDefault(projectId, BigDecimal.ZERO);
            BigDecimal totalExpense = expenseByProject.getOrDefault(projectId, BigDecimal.ZERO);
            BigDecimal profit = totalIncome.subtract(totalExpense);

            vo.setTotalIncome(totalIncome);
            vo.setTotalExpense(totalExpense);
            vo.setProfit(profit);

            if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
                vo.setProfitRate(profit.multiply(new BigDecimal("100"))
                        .divide(totalIncome, 2, RoundingMode.HALF_UP));
            } else {
                vo.setProfitRate(BigDecimal.ZERO);
            }

            results.add(vo);
        }

        ReportVO report = new ReportVO();
        report.setReportType(REPORT_TYPE_INCOME_EXPENSE);
        report.setReportName("收支对比报表");
        report.setPeriod(StringUtils.hasText(query.getPeriod()) ? query.getPeriod() : "all");
        report.setGeneratedAt(LocalDateTime.now());
        report.setData(results);
        return report;
    }

    // ==================== PROCUREMENT REPORT ====================

    @Override
    @Transactional(readOnly = true)
    public ReportVO getProcurementReport(ReportQueryDTO query) {
        // Stub implementation - biz_purchase_item mapper may not be directly available
        // Return placeholder structure with project-level aggregation
        List<Map<String, Object>> results = new ArrayList<>();

        if (query.getProjectId() != null) {
            BizProject project = projectMapper.selectById(query.getProjectId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("projectId", query.getProjectId());
            item.put("projectName", project != null ? project.getProjectName() : "未知项目");
            item.put("totalItems", 0);
            item.put("totalAmount", BigDecimal.ZERO);
            item.put("completedRate", BigDecimal.ZERO);
            results.add(item);
        } else {
            // List all active projects
            LambdaQueryWrapper<BizProject> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(BizProject::getStatus, 3, 4, 5); // approved/executing statuses
            List<BizProject> projects = projectMapper.selectList(wrapper);
            for (BizProject project : projects) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("projectId", project.getId());
                item.put("projectName", project.getProjectName());
                item.put("totalItems", 0);
                item.put("totalAmount", BigDecimal.ZERO);
                item.put("completedRate", BigDecimal.ZERO);
                results.add(item);
            }
        }

        ReportVO report = new ReportVO();
        report.setReportType(REPORT_TYPE_PROCUREMENT);
        report.setReportName("采购统计报表");
        report.setPeriod(StringUtils.hasText(query.getPeriod()) ? query.getPeriod() : "all");
        report.setGeneratedAt(LocalDateTime.now());
        report.setData(results);
        return report;
    }

    // ==================== INVENTORY REPORT ====================

    @Override
    @Transactional(readOnly = true)
    public ReportVO getInventoryReport(ReportQueryDTO query) {
        // Stub implementation - biz_material_inventory mapper may not be directly available
        // Return placeholder structure with project-level aggregation
        List<Map<String, Object>> results = new ArrayList<>();

        if (query.getProjectId() != null) {
            BizProject project = projectMapper.selectById(query.getProjectId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("projectId", query.getProjectId());
            item.put("projectName", project != null ? project.getProjectName() : "未知项目");
            item.put("materialCount", 0);
            item.put("totalQuantity", BigDecimal.ZERO);
            item.put("totalValue", BigDecimal.ZERO);
            results.add(item);
        } else {
            LambdaQueryWrapper<BizProject> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(BizProject::getStatus, 3, 4, 5);
            List<BizProject> projects = projectMapper.selectList(wrapper);
            for (BizProject project : projects) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("projectId", project.getId());
                item.put("projectName", project.getProjectName());
                item.put("materialCount", 0);
                item.put("totalQuantity", BigDecimal.ZERO);
                item.put("totalValue", BigDecimal.ZERO);
                results.add(item);
            }
        }

        ReportVO report = new ReportVO();
        report.setReportType(REPORT_TYPE_INVENTORY);
        report.setReportName("库存统计报表");
        report.setPeriod(StringUtils.hasText(query.getPeriod()) ? query.getPeriod() : "all");
        report.setGeneratedAt(LocalDateTime.now());
        report.setData(results);
        return report;
    }

    // ==================== PRE-COMPUTE ====================

    @Override
    @Transactional
    public void preComputeReports(String period) {
        log.info("开始预计算报表, period={}", period);

        ReportQueryDTO query = new ReportQueryDTO();
        query.setPeriod(period);

        // Generate all 4 report types
        saveSnapshot(REPORT_TYPE_COST, period, getProjectCostReport(query));
        saveSnapshot(REPORT_TYPE_INCOME_EXPENSE, period, getIncomeExpenseReport(query));
        saveSnapshot(REPORT_TYPE_PROCUREMENT, period, getProcurementReport(query));
        saveSnapshot(REPORT_TYPE_INVENTORY, period, getInventoryReport(query));

        log.info("报表预计算完成, period={}", period);
    }

    private void saveSnapshot(String reportType, String period, ReportVO report) {
        try {
            String dataJson = objectMapper.writeValueAsString(report.getData());

            BizReportSnapshot snapshot = new BizReportSnapshot();
            snapshot.setReportType(reportType);
            snapshot.setPeriod(period);
            snapshot.setDataJson(dataJson);
            snapshot.setGeneratedAt(LocalDateTime.now());
            snapshot.setCreatedAt(LocalDateTime.now());
            reportSnapshotMapper.insert(snapshot);

            log.info("报表快照已保存: type={}, period={}", reportType, period);
        } catch (Exception e) {
            log.error("保存报表快照失败: type={}, period={}", reportType, period, e);
            throw new BusinessException("保存报表快照失败: " + e.getMessage());
        }
    }

    // ==================== LIST CACHED REPORTS ====================

    @Override
    @Transactional(readOnly = true)
    public List<ReportVO> listCachedReports(String reportType) {
        LambdaQueryWrapper<BizReportSnapshot> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(reportType)) {
            wrapper.eq(BizReportSnapshot::getReportType, reportType);
        }
        wrapper.orderByDesc(BizReportSnapshot::getGeneratedAt);

        List<BizReportSnapshot> snapshots = reportSnapshotMapper.selectList(wrapper);
        List<ReportVO> results = new ArrayList<>();

        for (BizReportSnapshot snapshot : snapshots) {
            ReportVO vo = new ReportVO();
            vo.setReportType(snapshot.getReportType());
            vo.setReportName(getReportName(snapshot.getReportType()));
            vo.setPeriod(snapshot.getPeriod());
            vo.setGeneratedAt(snapshot.getGeneratedAt());

            try {
                Object data = objectMapper.readValue(snapshot.getDataJson(), new TypeReference<List<Map<String, Object>>>() {});
                vo.setData(data);
            } catch (Exception e) {
                log.warn("解析报表快照数据失败: id={}", snapshot.getId(), e);
                vo.setData(snapshot.getDataJson());
            }

            results.add(vo);
        }

        return results;
    }

    // ==================== HELPERS ====================

    private String getReportName(String reportType) {
        if (reportType == null) return "未知报表";
        return switch (reportType) {
            case REPORT_TYPE_COST -> "项目成本汇总报表";
            case REPORT_TYPE_INCOME_EXPENSE -> "收支对比报表";
            case REPORT_TYPE_PROCUREMENT -> "采购统计报表";
            case REPORT_TYPE_INVENTORY -> "库存统计报表";
            default -> "未知报表";
        };
    }
}
