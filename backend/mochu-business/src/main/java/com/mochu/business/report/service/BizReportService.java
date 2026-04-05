package com.mochu.business.report.service;

import com.mochu.business.report.dto.ReportQueryDTO;
import com.mochu.business.report.vo.ReportVO;

import java.util.List;

public interface BizReportService {

    ReportVO getProjectCostReport(ReportQueryDTO query);

    ReportVO getIncomeExpenseReport(ReportQueryDTO query);

    ReportVO getProcurementReport(ReportQueryDTO query);

    ReportVO getInventoryReport(ReportQueryDTO query);

    void preComputeReports(String period);

    List<ReportVO> listCachedReports(String reportType);
}
