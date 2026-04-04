package com.mochu.business.change.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.business.change.dto.*;
import com.mochu.business.change.entity.BizChangeLaborVisa;
import com.mochu.business.change.entity.BizChangeOwner;
import com.mochu.business.change.entity.BizChangeSiteVisa;
import com.mochu.business.change.mapper.BizChangeLaborVisaMapper;
import com.mochu.business.change.mapper.BizChangeOwnerMapper;
import com.mochu.business.change.mapper.BizChangeSiteVisaMapper;
import com.mochu.business.change.service.BizChangeService;
import com.mochu.business.change.vo.*;
import com.mochu.business.project.entity.BizProject;
import com.mochu.business.project.mapper.BizProjectMapper;
import com.mochu.common.constant.Constants;
import com.mochu.common.exception.BusinessException;
import com.mochu.common.result.PageResult;
import com.mochu.common.utils.BizNoGenerator;
import com.mochu.common.utils.SecurityUtils;
import com.mochu.system.entity.SysUser;
import com.mochu.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizChangeServiceImpl implements BizChangeService {

    private final BizChangeSiteVisaMapper siteVisaMapper;
    private final BizChangeOwnerMapper ownerChangeMapper;
    private final BizChangeLaborVisaMapper laborVisaMapper;
    private final BizProjectMapper projectMapper;
    private final SysUserMapper userMapper;
    private final BizNoGenerator bizNoGenerator;

    private static final Map<Integer, String> CHANGE_STATUS = Map.of(
            1, "草稿", 2, "待审批", 3, "已批准", 4, "已驳回"
    );

    private static final Map<String, String> CHANGE_TYPE_NAME = Map.of(
            "site_visa", "现场签证",
            "owner_change", "业主变更",
            "labor_visa", "人工签证"
    );

    // ==================== SITE VISA ====================

    @Override
    @Transactional
    public Long createSiteVisa(SiteVisaCreateDTO dto) {
        validateProject(dto.getProjectId());

        BizChangeSiteVisa visa = new BizChangeSiteVisa();
        visa.setProjectId(dto.getProjectId());
        visa.setVisaNo(bizNoGenerator.generateSiteVisaNo());
        visa.setDescription(dto.getDescription());
        visa.setAmount(dto.getAmount());
        visa.setFileUrl(dto.getFileUrl());
        visa.setStatus(2); // Submit directly to pending approval
        visa.setCreatorId(SecurityUtils.getCurrentUserId());
        siteVisaMapper.insert(visa);

        log.info("Site visa created: {}", visa.getVisaNo());
        return visa.getId();
    }

    @Override
    @Transactional
    public void approveSiteVisa(Long id, String comment) {
        validateApproveComment(comment);
        BizChangeSiteVisa visa = siteVisaMapper.selectById(id);
        if (visa == null) throw new BusinessException(404, "现场签证不存在");
        if (visa.getStatus() != 2) throw new BusinessException("当前状态不支持审批");

        visa.setStatus(3);
        visa.setApproverId(SecurityUtils.getCurrentUserId());
        visa.setApprovedAt(LocalDateTime.now());
        siteVisaMapper.updateById(visa);

        log.info("Site visa approved: {}", visa.getVisaNo());
    }

    @Override
    @Transactional
    public void rejectSiteVisa(Long id, String comment) {
        validateRejectComment(comment);
        BizChangeSiteVisa visa = siteVisaMapper.selectById(id);
        if (visa == null) throw new BusinessException(404, "现场签证不存在");
        if (visa.getStatus() != 2) throw new BusinessException("当前状态不支持驳回");

        visa.setStatus(4);
        visa.setApproverId(SecurityUtils.getCurrentUserId());
        visa.setApprovedAt(LocalDateTime.now());
        siteVisaMapper.updateById(visa);

        log.info("Site visa rejected: {}", visa.getVisaNo());
    }

    // ==================== OWNER CHANGE ====================

    @Override
    @Transactional
    public Long createOwnerChange(OwnerChangeCreateDTO dto) {
        validateProject(dto.getProjectId());

        BizChangeOwner change = new BizChangeOwner();
        change.setProjectId(dto.getProjectId());
        change.setChangeNo(bizNoGenerator.generateOwnerChangeNo());
        change.setDescription(dto.getDescription());
        change.setAmountChange(dto.getAmountChange());
        change.setFileUrl(dto.getFileUrl());
        change.setStatus(2);
        change.setCreatorId(SecurityUtils.getCurrentUserId());
        ownerChangeMapper.insert(change);

        log.info("Owner change created: {}", change.getChangeNo());
        return change.getId();
    }

    @Override
    @Transactional
    public void approveOwnerChange(Long id, String comment) {
        validateApproveComment(comment);
        BizChangeOwner change = ownerChangeMapper.selectById(id);
        if (change == null) throw new BusinessException(404, "业主变更不存在");
        if (change.getStatus() != 2) throw new BusinessException("当前状态不支持审批");

        change.setStatus(3);
        change.setApproverId(SecurityUtils.getCurrentUserId());
        change.setApprovedAt(LocalDateTime.now());
        ownerChangeMapper.updateById(change);

        log.info("Owner change approved: {}", change.getChangeNo());
    }

    @Override
    @Transactional
    public void rejectOwnerChange(Long id, String comment) {
        validateRejectComment(comment);
        BizChangeOwner change = ownerChangeMapper.selectById(id);
        if (change == null) throw new BusinessException(404, "业主变更不存在");
        if (change.getStatus() != 2) throw new BusinessException("当前状态不支持驳回");

        change.setStatus(4);
        change.setApproverId(SecurityUtils.getCurrentUserId());
        change.setApprovedAt(LocalDateTime.now());
        ownerChangeMapper.updateById(change);

        log.info("Owner change rejected: {}", change.getChangeNo());
    }

    // ==================== LABOR VISA ====================

    @Override
    @Transactional
    public Long createLaborVisa(LaborVisaCreateDTO dto) {
        validateProject(dto.getProjectId());

        BizChangeLaborVisa visa = new BizChangeLaborVisa();
        visa.setProjectId(dto.getProjectId());
        visa.setVisaNo(bizNoGenerator.generateSiteVisaNo()); // shared VS prefix with site visa
        visa.setDescription(dto.getDescription());
        visa.setLaborCount(dto.getLaborCount());
        visa.setAmount(dto.getAmount());
        visa.setStatus(2);
        visa.setCreatorId(SecurityUtils.getCurrentUserId());
        laborVisaMapper.insert(visa);

        log.info("Labor visa created: {}", visa.getVisaNo());
        return visa.getId();
    }

    @Override
    @Transactional
    public void approveLaborVisa(Long id, String comment) {
        validateApproveComment(comment);
        BizChangeLaborVisa visa = laborVisaMapper.selectById(id);
        if (visa == null) throw new BusinessException(404, "人工签证不存在");
        if (visa.getStatus() != 2) throw new BusinessException("当前状态不支持审批");

        visa.setStatus(3);
        visa.setApproverId(SecurityUtils.getCurrentUserId());
        visa.setApprovedAt(LocalDateTime.now());
        laborVisaMapper.updateById(visa);

        log.info("Labor visa approved: {}", visa.getVisaNo());
    }

    @Override
    @Transactional
    public void rejectLaborVisa(Long id, String comment) {
        validateRejectComment(comment);
        BizChangeLaborVisa visa = laborVisaMapper.selectById(id);
        if (visa == null) throw new BusinessException(404, "人工签证不存在");
        if (visa.getStatus() != 2) throw new BusinessException("当前状态不支持驳回");

        visa.setStatus(4);
        visa.setApproverId(SecurityUtils.getCurrentUserId());
        visa.setApprovedAt(LocalDateTime.now());
        laborVisaMapper.updateById(visa);

        log.info("Labor visa rejected: {}", visa.getVisaNo());
    }

    // ==================== CHANGE LEDGER ====================

    @Override
    @Transactional(readOnly = true)
    public PageResult<ChangeLedgerVO> queryChangeLedger(ChangeQueryDTO query) {
        int page = query.getPage() != null ? query.getPage() : Constants.DEFAULT_PAGE;
        int size = Math.min(query.getSize() != null ? query.getSize() : Constants.DEFAULT_SIZE, Constants.MAX_SIZE);

        List<ChangeLedgerVO> allRecords = new ArrayList<>();

        boolean querySiteVisa = !StringUtils.hasText(query.getChangeType()) || "site_visa".equals(query.getChangeType());
        boolean queryOwnerChange = !StringUtils.hasText(query.getChangeType()) || "owner_change".equals(query.getChangeType());
        boolean queryLaborVisa = !StringUtils.hasText(query.getChangeType()) || "labor_visa".equals(query.getChangeType());

        if (querySiteVisa) {
            LambdaQueryWrapper<BizChangeSiteVisa> svWrapper = new LambdaQueryWrapper<>();
            if (query.getProjectId() != null) svWrapper.eq(BizChangeSiteVisa::getProjectId, query.getProjectId());
            if (query.getStatus() != null) svWrapper.eq(BizChangeSiteVisa::getStatus, query.getStatus());
            List<BizChangeSiteVisa> siteVisas = siteVisaMapper.selectList(svWrapper);
            for (BizChangeSiteVisa sv : siteVisas) {
                ChangeLedgerVO vo = new ChangeLedgerVO();
                vo.setId(sv.getId());
                vo.setChangeType("site_visa");
                vo.setChangeTypeName(CHANGE_TYPE_NAME.get("site_visa"));
                vo.setProjectId(sv.getProjectId());
                vo.setProjectName(getProjectName(sv.getProjectId()));
                vo.setChangeNo(sv.getVisaNo());
                vo.setDescription(sv.getDescription());
                vo.setAmount(sv.getAmount());
                vo.setStatus(sv.getStatus());
                vo.setStatusName(CHANGE_STATUS.getOrDefault(sv.getStatus(), "未知"));
                vo.setCreatedAt(sv.getCreatedAt());
                allRecords.add(vo);
            }
        }

        if (queryOwnerChange) {
            LambdaQueryWrapper<BizChangeOwner> ocWrapper = new LambdaQueryWrapper<>();
            if (query.getProjectId() != null) ocWrapper.eq(BizChangeOwner::getProjectId, query.getProjectId());
            if (query.getStatus() != null) ocWrapper.eq(BizChangeOwner::getStatus, query.getStatus());
            List<BizChangeOwner> ownerChanges = ownerChangeMapper.selectList(ocWrapper);
            for (BizChangeOwner oc : ownerChanges) {
                ChangeLedgerVO vo = new ChangeLedgerVO();
                vo.setId(oc.getId());
                vo.setChangeType("owner_change");
                vo.setChangeTypeName(CHANGE_TYPE_NAME.get("owner_change"));
                vo.setProjectId(oc.getProjectId());
                vo.setProjectName(getProjectName(oc.getProjectId()));
                vo.setChangeNo(oc.getChangeNo());
                vo.setDescription(oc.getDescription());
                vo.setAmount(oc.getAmountChange());
                vo.setStatus(oc.getStatus());
                vo.setStatusName(CHANGE_STATUS.getOrDefault(oc.getStatus(), "未知"));
                vo.setCreatedAt(oc.getCreatedAt());
                allRecords.add(vo);
            }
        }

        if (queryLaborVisa) {
            LambdaQueryWrapper<BizChangeLaborVisa> lvWrapper = new LambdaQueryWrapper<>();
            if (query.getProjectId() != null) lvWrapper.eq(BizChangeLaborVisa::getProjectId, query.getProjectId());
            if (query.getStatus() != null) lvWrapper.eq(BizChangeLaborVisa::getStatus, query.getStatus());
            List<BizChangeLaborVisa> laborVisas = laborVisaMapper.selectList(lvWrapper);
            for (BizChangeLaborVisa lv : laborVisas) {
                ChangeLedgerVO vo = new ChangeLedgerVO();
                vo.setId(lv.getId());
                vo.setChangeType("labor_visa");
                vo.setChangeTypeName(CHANGE_TYPE_NAME.get("labor_visa"));
                vo.setProjectId(lv.getProjectId());
                vo.setProjectName(getProjectName(lv.getProjectId()));
                vo.setChangeNo(lv.getVisaNo());
                vo.setDescription(lv.getDescription());
                vo.setAmount(lv.getAmount());
                vo.setStatus(lv.getStatus());
                vo.setStatusName(CHANGE_STATUS.getOrDefault(lv.getStatus(), "未知"));
                vo.setCreatedAt(lv.getCreatedAt());
                allRecords.add(vo);
            }
        }

        // Sort by createdAt descending
        allRecords.sort(Comparator.comparing(ChangeLedgerVO::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));

        // Manual pagination
        long total = allRecords.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, allRecords.size());
        List<ChangeLedgerVO> pageRecords = fromIndex < allRecords.size()
                ? allRecords.subList(fromIndex, toIndex)
                : new ArrayList<>();

        return new PageResult<>(pageRecords, total, page, size);
    }

    // ==================== HELPER METHODS ====================

    private void validateProject(Long projectId) {
        BizProject project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(404, "项目不存在");
        }
    }

    private void validateApproveComment(String comment) {
        if (comment == null || comment.length() < 2) {
            throw new BusinessException("审批意见至少2个字符");
        }
    }

    private void validateRejectComment(String comment) {
        if (comment == null || comment.length() < 5) {
            throw new BusinessException("驳回意见至少5个字符");
        }
    }

    private String getProjectName(Long projectId) {
        if (projectId == null) return null;
        BizProject project = projectMapper.selectById(projectId);
        return project != null ? project.getProjectName() : null;
    }

    private String getUserRealName(Long userId) {
        if (userId == null) return null;
        SysUser user = userMapper.selectById(userId);
        return user != null ? user.getRealName() : null;
    }
}
