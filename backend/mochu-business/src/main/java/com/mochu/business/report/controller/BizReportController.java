package com.mochu.business.report.controller;

import com.mochu.business.report.dto.ReportQueryDTO;
import com.mochu.business.report.service.BizReportService;
import com.mochu.business.report.vo.ReportVO;
import com.mochu.common.result.R;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class BizReportController {

    private final BizReportService reportService;

    @GetMapping("/cost")
    @PreAuthorize("hasAuthority('report:view')")
    public R<ReportVO> getProjectCostReport(ReportQueryDTO query) {
        return R.ok(reportService.getProjectCostReport(query));
    }

    @GetMapping("/income-expense")
    @PreAuthorize("hasAuthority('report:view')")
    public R<ReportVO> getIncomeExpenseReport(ReportQueryDTO query) {
        return R.ok(reportService.getIncomeExpenseReport(query));
    }

    @GetMapping("/procurement")
    @PreAuthorize("hasAuthority('report:view')")
    public R<ReportVO> getProcurementReport(ReportQueryDTO query) {
        return R.ok(reportService.getProcurementReport(query));
    }

    @GetMapping("/inventory")
    @PreAuthorize("hasAuthority('report:view')")
    public R<ReportVO> getInventoryReport(ReportQueryDTO query) {
        return R.ok(reportService.getInventoryReport(query));
    }

    @PostMapping("/pre-compute")
    @PreAuthorize("hasAuthority('report:export')")
    public R<Void> preComputeReports(@RequestParam String period) {
        reportService.preComputeReports(period);
        return R.ok();
    }

    @GetMapping("/cached")
    @PreAuthorize("hasAuthority('report:view')")
    public R<List<ReportVO>> listCachedReports(@RequestParam(required = false) String reportType) {
        return R.ok(reportService.listCachedReports(reportType));
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasAuthority('report:export')")
    public void exportExcel(@RequestParam String reportType, ReportQueryDTO query,
                            HttpServletResponse response) {
        try {
            ReportVO report;
            switch (reportType) {
                case "project_cost" -> report = reportService.getProjectCostReport(query);
                case "income_expense" -> report = reportService.getIncomeExpenseReport(query);
                case "procurement" -> report = reportService.getProcurementReport(query);
                case "inventory" -> report = reportService.getInventoryReport(query);
                default -> throw new IllegalArgumentException("不支持的报表类型: " + reportType);
            }

            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("UTF-8");
            String fileName = report.getReportName() + "_" + report.getPeriod() + ".xls";
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));

            PrintWriter writer = response.getWriter();

            // Write header and data based on report type
            if (report.getData() instanceof List<?> dataList) {
                if ("project_cost".equals(reportType)) {
                    writer.println("项目ID\t项目名称\t材料费\t人工费\t设备费\t管理费\t其他费用\t总费用\t期间");
                    for (Object item : dataList) {
                        if (item instanceof Map<?, ?> map) {
                            writer.println(
                                    mapGet(map, "projectId") + "\t" +
                                    mapGet(map, "projectName") + "\t" +
                                    mapGet(map, "materialCost") + "\t" +
                                    mapGet(map, "laborCost") + "\t" +
                                    mapGet(map, "equipmentCost") + "\t" +
                                    mapGet(map, "managementCost") + "\t" +
                                    mapGet(map, "otherCost") + "\t" +
                                    mapGet(map, "totalCost") + "\t" +
                                    mapGet(map, "period"));
                        } else {
                            writeObjectFields(writer, item);
                        }
                    }
                } else if ("income_expense".equals(reportType)) {
                    writer.println("项目ID\t项目名称\t总收入\t总支出\t利润\t利润率(%)\t期间");
                    for (Object item : dataList) {
                        if (item instanceof Map<?, ?> map) {
                            writer.println(
                                    mapGet(map, "projectId") + "\t" +
                                    mapGet(map, "projectName") + "\t" +
                                    mapGet(map, "totalIncome") + "\t" +
                                    mapGet(map, "totalExpense") + "\t" +
                                    mapGet(map, "profit") + "\t" +
                                    mapGet(map, "profitRate") + "\t" +
                                    mapGet(map, "period"));
                        } else {
                            writeObjectFields(writer, item);
                        }
                    }
                } else if ("procurement".equals(reportType)) {
                    writer.println("项目ID\t项目名称\t采购项数\t采购总额\t完成率(%)");
                    for (Object item : dataList) {
                        if (item instanceof Map<?, ?> map) {
                            writer.println(
                                    mapGet(map, "projectId") + "\t" +
                                    mapGet(map, "projectName") + "\t" +
                                    mapGet(map, "totalItems") + "\t" +
                                    mapGet(map, "totalAmount") + "\t" +
                                    mapGet(map, "completedRate"));
                        } else {
                            writeObjectFields(writer, item);
                        }
                    }
                } else if ("inventory".equals(reportType)) {
                    writer.println("项目ID\t项目名称\t物资种类数\t总数量\t总价值");
                    for (Object item : dataList) {
                        if (item instanceof Map<?, ?> map) {
                            writer.println(
                                    mapGet(map, "projectId") + "\t" +
                                    mapGet(map, "projectName") + "\t" +
                                    mapGet(map, "materialCount") + "\t" +
                                    mapGet(map, "totalQuantity") + "\t" +
                                    mapGet(map, "totalValue"));
                        } else {
                            writeObjectFields(writer, item);
                        }
                    }
                }
            }

            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败: " + e.getMessage(), e);
        }
    }

    private String mapGet(Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private void writeObjectFields(PrintWriter writer, Object obj) {
        // Fallback: use reflection-free toString for non-map objects
        writer.println(obj.toString());
    }
}
