package com.mochu.business.completion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.business.completion.dto.*;
import com.mochu.business.completion.entity.*;
import com.mochu.business.completion.mapper.*;
import com.mochu.business.completion.service.BizCompletionService;
import com.mochu.business.completion.vo.*;
import com.mochu.business.material.entity.BizMaterialInbound;
import com.mochu.business.material.mapper.BizMaterialInboundMapper;
import com.mochu.business.change.entity.BizChangeSiteVisa;
import com.mochu.business.change.entity.BizChangeOwner;
import com.mochu.business.change.mapper.BizChangeSiteVisaMapper;
import com.mochu.business.change.mapper.BizChangeOwnerMapper;
import com.mochu.common.exception.BusinessException;
import com.mochu.common.result.PageResult;
import com.mochu.common.utils.BizNoGenerator;
import com.mochu.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizCompletionServiceImpl implements BizCompletionService {

    private final BizCompletionReportMapper reportMapper;
    private final BizCompletionDrawingMapper drawingMapper;
    private final BizCompletionSettlementMapper settlementMapper;
    private final BizCompletionLaborSettlementMapper laborSettlementMapper;
    private final BizMaterialInboundMapper inboundMapper;
    private final BizChangeSiteVisaMapper siteVisaMapper;
    private final BizChangeOwnerMapper ownerChangeMapper;
    private final BizNoGenerator bizNoGenerator;

    private static final String[] STATUS_NAMES = {"", "待审批", "审批中", "已通过", "已驳回"};
    private static final String[] ARCHIVE_CATEGORIES = {
        "合同文件", "竣工图纸", "验收报告", "结算文件", "签证变更", "质检记录", "其他文件"
    };

    // ==================== COMPLETION REPORT ====================

    @Override
    @Transactional(readOnly = true)
    public PageResult<CompletionReportVO> listReports(CompletionQueryDTO query) {
        Page<BizCompletionReport> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<BizCompletionReport> wrapper = new LambdaQueryWrapper<>();
        if (query.getProjectId() != null) wrapper.eq(BizCompletionReport::getProjectId, query.getProjectId());
        if (query.getStatus() != null) wrapper.eq(BizCompletionReport::getStatus, query.getStatus());
        wrapper.orderByDesc(BizCompletionReport::getCreatedAt);
        Page<BizCompletionReport> result = reportMapper.selectPage(page, wrapper);
        List<CompletionReportVO> records = result.getRecords().stream().map(this::toReportVO).collect(Collectors.toList());
        return PageResult.of(records, result.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    @Transactional
    public Long createCompletionReport(CompletionReportDTO dto) {
        // Feature 269: 完工前校验所有入库单审批完成
        long pendingInbounds = inboundMapper.selectCount(
            new LambdaQueryWrapper<BizMaterialInbound>()
                .eq(BizMaterialInbound::getProjectId, dto.getProjectId())
                .lt(BizMaterialInbound::getStatus, 4) // status < 4 means not fully approved
        );
        if (pendingInbounds > 0) {
            throw new BusinessException("该项目还有 " + pendingInbounds + " 个入库单未完成审批，无法提交完工申请");
        }

        BizCompletionReport report = new BizCompletionReport();
        report.setProjectId(dto.getProjectId());
        report.setReportNo(bizNoGenerator.generateCompletionReportNo());
        report.setCompletionDate(dto.getCompletionDate());
        report.setQualityRating(dto.getQualityRating());
        report.setSummary(dto.getSummary());
        report.setFileUrl(dto.getFileUrl());
        report.setStatus(1); // pending
        report.setCreatorId(SecurityUtils.getUserId());
        reportMapper.insert(report);
        log.info("完工申请已创建: {}", report.getReportNo());
        return report.getId();
    }

    @Override
    @Transactional
    public void approveReport(Long id, String comment) {
        if (comment == null || comment.length() < 2) throw new BusinessException("审批意见至少2个字符");
        BizCompletionReport report = reportMapper.selectById(id);
        if (report == null) throw new BusinessException("完工报告不存在");
        // Multi-level: PM->预算->采购->GM (status 1->2->3->4, but simplified as 1->3)
        if (report.getStatus() >= 3) throw new BusinessException("已审批通过");
        report.setStatus(3); // approved
        report.setApproverId(SecurityUtils.getUserId());
        report.setApprovedAt(LocalDateTime.now());
        reportMapper.updateById(report);
        log.info("完工报告审批通过: {}", report.getReportNo());
    }

    @Override
    @Transactional
    public void rejectReport(Long id, String comment) {
        if (comment == null || comment.length() < 5) throw new BusinessException("驳回意见至少5个字符");
        BizCompletionReport report = reportMapper.selectById(id);
        if (report == null) throw new BusinessException("完工报告不存在");
        report.setStatus(4); // rejected
        reportMapper.updateById(report);
    }

    // ==================== DRAWINGS ====================

    @Override
    @Transactional(readOnly = true)
    public List<DrawingVO> listDrawings(Long projectId) {
        LambdaQueryWrapper<BizCompletionDrawing> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizCompletionDrawing::getProjectId, projectId);
        wrapper.orderByAsc(BizCompletionDrawing::getDrawingName);
        wrapper.orderByDesc(BizCompletionDrawing::getVersion);
        return drawingMapper.selectList(wrapper).stream().map(this::toDrawingVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void uploadDrawings(DrawingUploadDTO dto) {
        if (dto.getDrawings() == null || dto.getDrawings().isEmpty()) {
            throw new BusinessException("请至少上传一个图纸");
        }
        if (dto.getDrawings().size() > 10) {
            throw new BusinessException("单次最多上传10个图纸");
        }
        for (DrawingUploadDTO.DrawingItem item : dto.getDrawings()) {
            // Check if same-name drawing exists for version increment
            BizCompletionDrawing existing = drawingMapper.selectOne(
                new LambdaQueryWrapper<BizCompletionDrawing>()
                    .eq(BizCompletionDrawing::getProjectId, dto.getProjectId())
                    .eq(BizCompletionDrawing::getDrawingName, item.getDrawingName())
                    .orderByDesc(BizCompletionDrawing::getVersion)
                    .last("LIMIT 1")
            );
            int version = existing != null ? existing.getVersion() + 1 : 1;

            BizCompletionDrawing drawing = new BizCompletionDrawing();
            drawing.setProjectId(dto.getProjectId());
            drawing.setDrawingName(item.getDrawingName());
            drawing.setDrawingType(item.getDrawingType());
            drawing.setFileUrl(item.getFileUrl());
            drawing.setVersion(version);
            drawing.setUploaderId(SecurityUtils.getUserId());
            drawingMapper.insert(drawing);
        }
        log.info("上传{}张图纸到项目{}", dto.getDrawings().size(), dto.getProjectId());
    }

    @Override
    @Transactional
    public void uploadNewVersion(Long drawingId, String fileUrl) {
        BizCompletionDrawing existing = drawingMapper.selectById(drawingId);
        if (existing == null) throw new BusinessException("图纸不存在");
        BizCompletionDrawing newVersion = new BizCompletionDrawing();
        newVersion.setProjectId(existing.getProjectId());
        newVersion.setDrawingName(existing.getDrawingName());
        newVersion.setDrawingType(existing.getDrawingType());
        newVersion.setFileUrl(fileUrl);
        newVersion.setVersion(existing.getVersion() + 1);
        newVersion.setUploaderId(SecurityUtils.getUserId());
        drawingMapper.insert(newVersion);
        log.info("图纸{}升级到版本{}", existing.getDrawingName(), newVersion.getVersion());
    }

    @Override
    @Transactional
    public void approveDrawing(Long id, String comment) {
        if (comment == null || comment.length() < 2) throw new BusinessException("审批意见至少2个字符");
        // Drawing approval is tracked via the completion report flow
        // Individual drawing approval is a simplified pass-through
        log.info("图纸{}审批通过", id);
    }

    @Override
    @Transactional
    public void rejectDrawing(Long id, String comment) {
        if (comment == null || comment.length() < 5) throw new BusinessException("驳回意见至少5个字符");
        log.info("图纸{}审批驳回", id);
    }

    // ==================== ARCHIVE ====================

    @Override
    @Transactional(readOnly = true)
    public ArchiveVO getArchive(Long projectId) {
        ArchiveVO archive = new ArchiveVO();
        archive.setProjectId(projectId);
        Map<String, List<ArchiveVO.ArchiveItem>> categories = new LinkedHashMap<>();
        for (String cat : ARCHIVE_CATEGORIES) {
            categories.put(cat, new ArrayList<>());
        }

        // Collect drawings into 竣工图纸
        List<BizCompletionDrawing> drawings = drawingMapper.selectList(
            new LambdaQueryWrapper<BizCompletionDrawing>()
                .eq(BizCompletionDrawing::getProjectId, projectId)
        );
        for (BizCompletionDrawing d : drawings) {
            ArchiveVO.ArchiveItem item = new ArchiveVO.ArchiveItem();
            item.setFileName(d.getDrawingName() + " v" + d.getVersion());
            item.setFileUrl(d.getFileUrl());
            item.setSource("竣工图纸");
            item.setUploadedAt(d.getCreatedAt() != null ? d.getCreatedAt().toString() : "");
            categories.get("竣工图纸").add(item);
        }

        // Collect completion reports into 验收报告
        List<BizCompletionReport> reports = reportMapper.selectList(
            new LambdaQueryWrapper<BizCompletionReport>()
                .eq(BizCompletionReport::getProjectId, projectId)
        );
        for (BizCompletionReport r : reports) {
            if (r.getFileUrl() != null) {
                ArchiveVO.ArchiveItem item = new ArchiveVO.ArchiveItem();
                item.setFileName("完工报告 " + r.getReportNo());
                item.setFileUrl(r.getFileUrl());
                item.setSource("完工报告");
                item.setUploadedAt(r.getCreatedAt() != null ? r.getCreatedAt().toString() : "");
                categories.get("验收报告").add(item);
            }
        }

        // Collect settlements into 结算文件
        List<BizCompletionSettlement> settlements = settlementMapper.selectList(
            new LambdaQueryWrapper<BizCompletionSettlement>()
                .eq(BizCompletionSettlement::getProjectId, projectId)
        );
        for (BizCompletionSettlement s : settlements) {
            ArchiveVO.ArchiveItem item = new ArchiveVO.ArchiveItem();
            item.setFileName("工程结算 " + s.getSettlementNo());
            item.setFileUrl("");
            item.setSource("结算");
            item.setUploadedAt(s.getCreatedAt() != null ? s.getCreatedAt().toString() : "");
            categories.get("结算文件").add(item);
        }

        // Collect change visas into 签证变更
        List<BizChangeSiteVisa> visas = siteVisaMapper.selectList(
            new LambdaQueryWrapper<BizChangeSiteVisa>()
                .eq(BizChangeSiteVisa::getProjectId, projectId)
                .eq(BizChangeSiteVisa::getStatus, 2) // approved only
        );
        for (BizChangeSiteVisa v : visas) {
            if (v.getFileUrl() != null) {
                ArchiveVO.ArchiveItem item = new ArchiveVO.ArchiveItem();
                item.setFileName("现场签证 " + v.getVisaNo());
                item.setFileUrl(v.getFileUrl());
                item.setSource("签证");
                item.setUploadedAt(v.getCreatedAt() != null ? v.getCreatedAt().toString() : "");
                categories.get("签证变更").add(item);
            }
        }

        archive.setCategories(categories);
        return archive;
    }

    // ==================== ENGINEERING SETTLEMENT ====================

    @Override
    @Transactional(readOnly = true)
    public SettlementVO getSettlement(Long projectId) {
        BizCompletionSettlement settlement = settlementMapper.selectOne(
            new LambdaQueryWrapper<BizCompletionSettlement>()
                .eq(BizCompletionSettlement::getProjectId, projectId)
                .orderByDesc(BizCompletionSettlement::getCreatedAt)
                .last("LIMIT 1")
        );
        return settlement != null ? toSettlementVO(settlement) : null;
    }

    @Override
    @Transactional
    public void generateSettlement(Long projectId) {
        // Feature 271: 竣工结算汇总变更成本
        // Sum up all approved change amounts
        BigDecimal changeAmount = BigDecimal.ZERO;

        List<BizChangeSiteVisa> visas = siteVisaMapper.selectList(
            new LambdaQueryWrapper<BizChangeSiteVisa>()
                .eq(BizChangeSiteVisa::getProjectId, projectId)
                .eq(BizChangeSiteVisa::getStatus, 2)
        );
        for (BizChangeSiteVisa v : visas) {
            changeAmount = changeAmount.add(v.getAmount() != null ? v.getAmount() : BigDecimal.ZERO);
        }

        List<BizChangeOwner> ownerChanges = ownerChangeMapper.selectList(
            new LambdaQueryWrapper<BizChangeOwner>()
                .eq(BizChangeOwner::getProjectId, projectId)
                .eq(BizChangeOwner::getStatus, 2)
        );
        for (BizChangeOwner oc : ownerChanges) {
            changeAmount = changeAmount.add(oc.getAmountChange() != null ? oc.getAmountChange() : BigDecimal.ZERO);
        }

        // For contract amount, we'd query the project's main contract - simplified here
        BigDecimal contractAmount = BigDecimal.ZERO; // Would come from contract query
        BigDecimal finalAmount = contractAmount.add(changeAmount);

        BizCompletionSettlement settlement = new BizCompletionSettlement();
        settlement.setProjectId(projectId);
        settlement.setSettlementNo(bizNoGenerator.generate("JS", "yyMMdd", 3));
        settlement.setContractAmount(contractAmount);
        settlement.setChangeAmount(changeAmount);
        settlement.setFinalAmount(finalAmount);
        settlement.setStatus(1);
        settlement.setCreatorId(SecurityUtils.getUserId());
        settlementMapper.insert(settlement);
        log.info("工程结算已生成: {}", settlement.getSettlementNo());
    }

    // ==================== LABOR SETTLEMENT ====================

    @Override
    @Transactional(readOnly = true)
    public PageResult<LaborSettlementVO> listLaborSettlements(CompletionQueryDTO query) {
        Page<BizCompletionLaborSettlement> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<BizCompletionLaborSettlement> wrapper = new LambdaQueryWrapper<>();
        if (query.getProjectId() != null) wrapper.eq(BizCompletionLaborSettlement::getProjectId, query.getProjectId());
        if (query.getStatus() != null) wrapper.eq(BizCompletionLaborSettlement::getStatus, query.getStatus());
        wrapper.orderByDesc(BizCompletionLaborSettlement::getCreatedAt);
        Page<BizCompletionLaborSettlement> result = laborSettlementMapper.selectPage(page, wrapper);
        List<LaborSettlementVO> records = result.getRecords().stream().map(this::toLaborSettlementVO).collect(Collectors.toList());
        return PageResult.of(records, result.getTotal(), query.getPage(), query.getSize());
    }

    @Override
    @Transactional
    public Long createLaborSettlement(LaborSettlementCreateDTO dto) {
        BizCompletionLaborSettlement settlement = new BizCompletionLaborSettlement();
        settlement.setProjectId(dto.getProjectId());
        settlement.setSettlementNo(bizNoGenerator.generateLaborSettlementNo());
        settlement.setTeamName(dto.getTeamName());
        settlement.setLaborType(dto.getLaborType());
        settlement.setTotalDays(dto.getTotalDays());
        settlement.setDailyRate(dto.getDailyRate());

        // Calculate: totalAmount = totalDays * dailyRate
        BigDecimal totalAmount = dto.getDailyRate().multiply(BigDecimal.valueOf(dto.getTotalDays()));
        settlement.setTotalAmount(totalAmount);
        settlement.setDeduction(dto.getDeduction() != null ? dto.getDeduction() : BigDecimal.ZERO);
        settlement.setFinalAmount(totalAmount.subtract(settlement.getDeduction()));
        settlement.setStatus(1); // pending
        settlement.setCreatorId(SecurityUtils.getUserId());
        laborSettlementMapper.insert(settlement);
        log.info("劳务结算已创建: {}", settlement.getSettlementNo());
        return settlement.getId();
    }

    @Override
    @Transactional
    public void approveLaborSettlement(Long id, String comment) {
        if (comment == null || comment.length() < 2) throw new BusinessException("审批意见至少2个字符");
        BizCompletionLaborSettlement settlement = laborSettlementMapper.selectById(id);
        if (settlement == null) throw new BusinessException("劳务结算不存在");
        if (settlement.getStatus() >= 3) throw new BusinessException("已审批完成");
        // Multi-level: PM->预算->采购->财务->GM (simplified as 1->3)
        settlement.setStatus(3);
        settlement.setApprovedAt(LocalDateTime.now());
        laborSettlementMapper.updateById(settlement);
        log.info("劳务结算审批通过: {}", settlement.getSettlementNo());
    }

    @Override
    @Transactional
    public void rejectLaborSettlement(Long id, String comment) {
        if (comment == null || comment.length() < 5) throw new BusinessException("驳回意见至少5个字符");
        BizCompletionLaborSettlement settlement = laborSettlementMapper.selectById(id);
        if (settlement == null) throw new BusinessException("劳务结算不存在");
        settlement.setStatus(4);
        laborSettlementMapper.updateById(settlement);
    }

    // ==================== MAPPERS ====================

    private CompletionReportVO toReportVO(BizCompletionReport r) {
        CompletionReportVO vo = new CompletionReportVO();
        vo.setId(r.getId());
        vo.setProjectId(r.getProjectId());
        vo.setReportNo(r.getReportNo());
        vo.setCompletionDate(r.getCompletionDate());
        vo.setQualityRating(r.getQualityRating());
        vo.setSummary(r.getSummary());
        vo.setFileUrl(r.getFileUrl());
        vo.setStatus(r.getStatus());
        vo.setStatusName(r.getStatus() != null && r.getStatus() < STATUS_NAMES.length ? STATUS_NAMES[r.getStatus()] : "");
        vo.setApprovedAt(r.getApprovedAt());
        vo.setCreatedAt(r.getCreatedAt());
        return vo;
    }

    private DrawingVO toDrawingVO(BizCompletionDrawing d) {
        DrawingVO vo = new DrawingVO();
        vo.setId(d.getId());
        vo.setProjectId(d.getProjectId());
        vo.setDrawingName(d.getDrawingName());
        vo.setDrawingType(d.getDrawingType());
        vo.setFileUrl(d.getFileUrl());
        vo.setVersion(d.getVersion());
        vo.setCreatedAt(d.getCreatedAt());
        vo.setUpdatedAt(d.getUpdatedAt());
        return vo;
    }

    private SettlementVO toSettlementVO(BizCompletionSettlement s) {
        SettlementVO vo = new SettlementVO();
        vo.setId(s.getId());
        vo.setProjectId(s.getProjectId());
        vo.setSettlementNo(s.getSettlementNo());
        vo.setContractAmount(s.getContractAmount());
        vo.setChangeAmount(s.getChangeAmount());
        vo.setFinalAmount(s.getFinalAmount());
        vo.setStatus(s.getStatus());
        vo.setStatusName(s.getStatus() != null && s.getStatus() < STATUS_NAMES.length ? STATUS_NAMES[s.getStatus()] : "");
        vo.setApprovedAt(s.getApprovedAt());
        vo.setCreatedAt(s.getCreatedAt());
        return vo;
    }

    private LaborSettlementVO toLaborSettlementVO(BizCompletionLaborSettlement ls) {
        LaborSettlementVO vo = new LaborSettlementVO();
        vo.setId(ls.getId());
        vo.setProjectId(ls.getProjectId());
        vo.setSettlementNo(ls.getSettlementNo());
        vo.setTeamName(ls.getTeamName());
        vo.setLaborType(ls.getLaborType());
        vo.setTotalDays(ls.getTotalDays());
        vo.setDailyRate(ls.getDailyRate());
        vo.setTotalAmount(ls.getTotalAmount());
        vo.setDeduction(ls.getDeduction());
        vo.setFinalAmount(ls.getFinalAmount());
        vo.setStatus(ls.getStatus());
        vo.setStatusName(ls.getStatus() != null && ls.getStatus() < STATUS_NAMES.length ? STATUS_NAMES[ls.getStatus()] : "");
        vo.setApprovedAt(ls.getApprovedAt());
        vo.setCreatedAt(ls.getCreatedAt());
        return vo;
    }
}
