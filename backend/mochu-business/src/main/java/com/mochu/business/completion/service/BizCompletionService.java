package com.mochu.business.completion.service;

import com.mochu.business.completion.dto.*;
import com.mochu.business.completion.vo.*;
import com.mochu.common.result.PageResult;

import java.util.List;

public interface BizCompletionService {
    // Completion Report
    PageResult<CompletionReportVO> listReports(CompletionQueryDTO query);
    Long createCompletionReport(CompletionReportDTO dto);
    void approveReport(Long id, String comment);
    void rejectReport(Long id, String comment);

    // Drawings
    List<DrawingVO> listDrawings(Long projectId);
    void uploadDrawings(DrawingUploadDTO dto);
    void uploadNewVersion(Long drawingId, String fileUrl);
    void approveDrawing(Long id, String comment);
    void rejectDrawing(Long id, String comment);

    // Archive
    ArchiveVO getArchive(Long projectId);

    // Engineering Settlement
    SettlementVO getSettlement(Long projectId);
    void generateSettlement(Long projectId);

    // Labor Settlement
    PageResult<LaborSettlementVO> listLaborSettlements(CompletionQueryDTO query);
    Long createLaborSettlement(LaborSettlementCreateDTO dto);
    void approveLaborSettlement(Long id, String comment);
    void rejectLaborSettlement(Long id, String comment);
}
