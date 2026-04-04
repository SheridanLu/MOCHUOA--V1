package com.mochu.business.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.business.project.dto.ProjectCreateDTO;
import com.mochu.business.project.dto.ProjectQueryDTO;
import com.mochu.business.project.dto.ProjectUpdateDTO;
import com.mochu.business.project.entity.BizProject;
import com.mochu.business.project.entity.BizProjectMember;
import com.mochu.business.project.entity.BizProjectPaymentBatch;
import com.mochu.business.project.mapper.BizProjectMapper;
import com.mochu.business.project.mapper.BizProjectMemberMapper;
import com.mochu.business.project.mapper.BizProjectPaymentBatchMapper;
import com.mochu.business.project.service.BizProjectService;
import com.mochu.business.project.vo.ProjectVO;
import com.mochu.common.constant.Constants;
import com.mochu.common.enums.ProjectStatus;
import com.mochu.common.exception.BusinessException;
import com.mochu.common.result.PageResult;
import com.mochu.common.utils.BizNoGenerator;
import com.mochu.common.utils.SecurityUtils;
import com.mochu.system.entity.SysDepartment;
import com.mochu.system.entity.SysUser;
import com.mochu.system.mapper.SysDepartmentMapper;
import com.mochu.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizProjectServiceImpl implements BizProjectService {

    private final BizProjectMapper projectMapper;
    private final BizProjectMemberMapper memberMapper;
    private final BizProjectPaymentBatchMapper paymentBatchMapper;
    private final SysUserMapper userMapper;
    private final SysDepartmentMapper departmentMapper;
    private final BizNoGenerator bizNoGenerator;

    private static final Map<Integer, String> PROJECT_TYPE_MAP = Map.of(
            1, "实体项目", 2, "虚拟项目"
    );

    @Override
    @Transactional(readOnly = true)
    public PageResult<ProjectVO> listProjects(ProjectQueryDTO query) {
        int page = query.getPage() != null ? query.getPage() : Constants.DEFAULT_PAGE;
        int size = Math.min(query.getSize() != null ? query.getSize() : Constants.DEFAULT_SIZE, Constants.MAX_SIZE);

        LambdaQueryWrapper<BizProject> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(BizProject::getProjectName, query.getKeyword())
                    .or().like(BizProject::getProjectNo, query.getKeyword()));
        }
        if (query.getProjectType() != null) wrapper.eq(BizProject::getProjectType, query.getProjectType());
        if (query.getStatus() != null) wrapper.eq(BizProject::getStatus, query.getStatus());
        if (query.getDeptId() != null) wrapper.eq(BizProject::getDeptId, query.getDeptId());
        wrapper.orderByDesc(BizProject::getCreatedAt);

        IPage<BizProject> pageResult = projectMapper.selectPage(new Page<>(page, size), wrapper);
        List<ProjectVO> voList = pageResult.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(voList, pageResult.getTotal(), page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectVO getProjectById(Long id) {
        BizProject project = projectMapper.selectById(id);
        if (project == null) throw new BusinessException(404, "项目不存在");
        ProjectVO vo = toVO(project);
        // Load payment batches
        LambdaQueryWrapper<BizProjectPaymentBatch> batchWrapper = new LambdaQueryWrapper<>();
        batchWrapper.eq(BizProjectPaymentBatch::getProjectId, id).orderByAsc(BizProjectPaymentBatch::getBatchNo);
        List<BizProjectPaymentBatch> batches = paymentBatchMapper.selectList(batchWrapper);
        vo.setPaymentBatches(batches.stream().map(b -> {
            ProjectVO.PaymentBatchVO bv = new ProjectVO.PaymentBatchVO();
            bv.setId(b.getId());
            bv.setBatchNo(b.getBatchNo());
            bv.setDescription(b.getDescription());
            bv.setRatio(b.getRatio());
            bv.setAmount(b.getAmount());
            bv.setPlannedDate(b.getPlannedDate() != null ? b.getPlannedDate().toString() : null);
            bv.setStatus(b.getStatus());
            return bv;
        }).collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional
    public Long createProject(ProjectCreateDTO dto) {
        // Validate tax consistency for entity projects
        if (dto.getProjectType() == 1) {
            validateTaxConsistency(dto.getBidAmount(), dto.getTaxRate(), dto.getTaxAmount(), dto.getAmountWithoutTax());
        }

        BizProject project = new BizProject();
        project.setProjectType(dto.getProjectType());
        project.setProjectName(dto.getProjectName());
        project.setDescription(dto.getDescription());
        project.setDeptId(dto.getDeptId());
        project.setOwnerId(SecurityUtils.getCurrentUserId());
        project.setStatus(ProjectStatus.DRAFT.getValue());

        // Generate project number
        if (dto.getProjectType() == 2) {
            project.setProjectNo(bizNoGenerator.generateVirtualProjectNo());
            project.setInvestLimit(dto.getInvestLimit());
            project.setStatus(ProjectStatus.TRACKING.getValue());
        } else {
            project.setProjectNo(bizNoGenerator.generateProjectNo());
            project.setBidAmount(dto.getBidAmount());
            project.setTaxRate(dto.getTaxRate());
            project.setTaxAmount(dto.getTaxAmount());
            project.setAmountWithoutTax(dto.getAmountWithoutTax());
            project.setBidNoticeUrl(dto.getBidNoticeUrl());
        }

        projectMapper.insert(project);

        // Save payment batches
        if (dto.getPaymentBatches() != null && !dto.getPaymentBatches().isEmpty()) {
            validatePaymentBatchRatio(dto.getPaymentBatches());
            for (ProjectCreateDTO.PaymentBatchDTO batchDTO : dto.getPaymentBatches()) {
                BizProjectPaymentBatch batch = new BizProjectPaymentBatch();
                batch.setProjectId(project.getId());
                batch.setBatchNo(batchDTO.getBatchNo());
                batch.setDescription(batchDTO.getDescription());
                batch.setRatio(batchDTO.getRatio());
                batch.setAmount(batchDTO.getAmount());
                if (batchDTO.getPlannedDate() != null) batch.setPlannedDate(LocalDate.parse(batchDTO.getPlannedDate()));
                batch.setStatus(1);
                paymentBatchMapper.insert(batch);
            }
        }

        // Add creator as project member
        BizProjectMember member = new BizProjectMember();
        member.setProjectId(project.getId());
        member.setUserId(SecurityUtils.getCurrentUserId());
        member.setRole("创建人");
        member.setJoinedAt(LocalDateTime.now());
        memberMapper.insert(member);

        log.info("Project created: {} ({})", project.getProjectNo(), project.getProjectName());
        return project.getId();
    }

    @Override
    @Transactional
    public void updateProject(Long id, ProjectUpdateDTO dto) {
        BizProject project = projectMapper.selectById(id);
        if (project == null) throw new BusinessException(404, "项目不存在");
        if (project.getStatus() != ProjectStatus.DRAFT.getValue()) {
            throw new BusinessException("只有草稿状态的项目可以编辑");
        }

        if (dto.getProjectName() != null) project.setProjectName(dto.getProjectName());
        if (dto.getDescription() != null) project.setDescription(dto.getDescription());
        if (dto.getDeptId() != null) project.setDeptId(dto.getDeptId());
        if (dto.getBidAmount() != null) project.setBidAmount(dto.getBidAmount());
        if (dto.getTaxRate() != null) project.setTaxRate(dto.getTaxRate());
        if (dto.getTaxAmount() != null) project.setTaxAmount(dto.getTaxAmount());
        if (dto.getAmountWithoutTax() != null) project.setAmountWithoutTax(dto.getAmountWithoutTax());
        if (dto.getInvestLimit() != null) project.setInvestLimit(dto.getInvestLimit());
        if (dto.getBidNoticeUrl() != null) project.setBidNoticeUrl(dto.getBidNoticeUrl());

        if (project.getProjectType() == 1) {
            validateTaxConsistency(project.getBidAmount(), project.getTaxRate(), project.getTaxAmount(), project.getAmountWithoutTax());
        }

        projectMapper.updateById(project);

        // Update payment batches
        if (dto.getPaymentBatches() != null) {
            validatePaymentBatchRatio(dto.getPaymentBatches());
            LambdaQueryWrapper<BizProjectPaymentBatch> delWrapper = new LambdaQueryWrapper<>();
            delWrapper.eq(BizProjectPaymentBatch::getProjectId, id);
            paymentBatchMapper.delete(delWrapper);
            for (ProjectCreateDTO.PaymentBatchDTO batchDTO : dto.getPaymentBatches()) {
                BizProjectPaymentBatch batch = new BizProjectPaymentBatch();
                batch.setProjectId(id);
                batch.setBatchNo(batchDTO.getBatchNo());
                batch.setDescription(batchDTO.getDescription());
                batch.setRatio(batchDTO.getRatio());
                batch.setAmount(batchDTO.getAmount());
                if (batchDTO.getPlannedDate() != null) batch.setPlannedDate(LocalDate.parse(batchDTO.getPlannedDate()));
                batch.setStatus(1);
                paymentBatchMapper.insert(batch);
            }
        }
    }

    @Override
    @Transactional
    public void submitForApproval(Long id) {
        BizProject project = projectMapper.selectById(id);
        if (project == null) throw new BusinessException(404, "项目不存在");
        if (project.getStatus() != ProjectStatus.DRAFT.getValue()) {
            throw new BusinessException("只有草稿状态的项目可以提交审批");
        }
        if (project.getProjectType() == 2) {
            throw new BusinessException("虚拟项目无需审批");
        }
        project.setStatus(ProjectStatus.PENDING.getValue());
        projectMapper.updateById(project);
        log.info("Project {} submitted for approval", project.getProjectNo());
    }

    @Override
    @Transactional
    public void approve(Long id, String comment) {
        BizProject project = projectMapper.selectById(id);
        if (project == null) throw new BusinessException(404, "项目不存在");
        if (project.getStatus() != ProjectStatus.PENDING.getValue()) {
            throw new BusinessException("只有待审批状态的项目可以审批通过");
        }
        if (comment == null || comment.length() < 2) {
            throw new BusinessException("审批意见至少2个字符");
        }
        project.setStatus(ProjectStatus.APPROVED.getValue());
        project.setApproverId(SecurityUtils.getCurrentUserId());
        project.setApprovedAt(LocalDateTime.now());
        projectMapper.updateById(project);
        log.info("Project {} approved", project.getProjectNo());
    }

    @Override
    @Transactional
    public void reject(Long id, String comment) {
        BizProject project = projectMapper.selectById(id);
        if (project == null) throw new BusinessException(404, "项目不存在");
        if (project.getStatus() != ProjectStatus.PENDING.getValue()) {
            throw new BusinessException("只有待审批状态的项目可以驳回");
        }
        if (comment == null || comment.length() < 5) {
            throw new BusinessException("驳回意见至少5个字符");
        }
        project.setStatus(ProjectStatus.DRAFT.getValue());
        projectMapper.updateById(project);
        log.info("Project {} rejected: {}", project.getProjectNo(), comment);
    }

    @Override
    @Transactional
    public void pause(Long id) {
        BizProject project = projectMapper.selectById(id);
        if (project == null) throw new BusinessException(404, "项目不存在");
        int s = project.getStatus();
        if (s != ProjectStatus.APPROVED.getValue() && s != ProjectStatus.IN_PROGRESS.getValue()) {
            throw new BusinessException("只有已审批或进行中的项目可以暂停");
        }
        project.setStatus(ProjectStatus.PAUSED.getValue());
        project.setPausedAt(LocalDateTime.now());
        projectMapper.updateById(project);
        log.info("Project {} paused", project.getProjectNo());
    }

    @Override
    @Transactional
    public void resume(Long id) {
        BizProject project = projectMapper.selectById(id);
        if (project == null) throw new BusinessException(404, "项目不存在");
        if (project.getStatus() != ProjectStatus.PAUSED.getValue()) {
            throw new BusinessException("只有已暂停的项目可以恢复");
        }
        project.setStatus(ProjectStatus.IN_PROGRESS.getValue());
        project.setPausedAt(null);
        projectMapper.updateById(project);
        log.info("Project {} resumed", project.getProjectNo());
    }

    @Override
    @Transactional
    public void close(Long id) {
        BizProject project = projectMapper.selectById(id);
        if (project == null) throw new BusinessException(404, "项目不存在");
        int s = project.getStatus();
        if (s != ProjectStatus.IN_PROGRESS.getValue() && s != ProjectStatus.APPROVED.getValue()) {
            throw new BusinessException("只有进行中或已审批的项目可以关闭");
        }
        project.setStatus(ProjectStatus.CLOSED.getValue());
        project.setClosedAt(LocalDateTime.now());
        projectMapper.updateById(project);
        log.info("Project {} closed", project.getProjectNo());
    }

    @Override
    @Transactional
    public void terminate(Long id, String reason) {
        BizProject project = projectMapper.selectById(id);
        if (project == null) throw new BusinessException(404, "项目不存在");
        if (project.getProjectType() != 2) {
            throw new BusinessException("只有虚拟项目可以中止");
        }
        if (project.getStatus() == ProjectStatus.TERMINATED.getValue() || project.getStatus() == ProjectStatus.CONVERTED.getValue()) {
            throw new BusinessException("项目已终结，不能再次中止");
        }
        project.setStatus(ProjectStatus.TERMINATED.getValue());
        project.setTerminationReason(reason);
        projectMapper.updateById(project);
        log.info("Virtual project {} terminated: {}", project.getProjectNo(), reason);
    }

    @Override
    @Transactional
    public void convertVirtualToEntity(Long virtualId, Long costTargetProjectId) {
        BizProject virtual = projectMapper.selectById(virtualId);
        if (virtual == null) throw new BusinessException(404, "虚拟项目不存在");
        if (virtual.getProjectType() != 2) throw new BusinessException("只能转换虚拟项目");
        if (virtual.getStatus() == ProjectStatus.TERMINATED.getValue() || virtual.getStatus() == ProjectStatus.CONVERTED.getValue()) {
            throw new BusinessException("项目已终结，不能转换");
        }

        // Create new entity project linked to virtual
        BizProject entity = new BizProject();
        entity.setProjectNo(bizNoGenerator.generateProjectNo());
        entity.setProjectName(virtual.getProjectName());
        entity.setProjectType(1);
        entity.setStatus(ProjectStatus.DRAFT.getValue());
        entity.setDescription(virtual.getDescription());
        entity.setOwnerId(virtual.getOwnerId());
        entity.setDeptId(virtual.getDeptId());
        entity.setCostTargetProjectId(virtualId);
        projectMapper.insert(entity);

        // Mark virtual as converted
        virtual.setStatus(ProjectStatus.CONVERTED.getValue());
        virtual.setCostTargetProjectId(entity.getId());
        projectMapper.updateById(virtual);

        log.info("Virtual project {} converted to entity project {}", virtual.getProjectNo(), entity.getProjectNo());
    }

    private void validateTaxConsistency(BigDecimal bidAmount, BigDecimal taxRate, BigDecimal taxAmount, BigDecimal amountWithoutTax) {
        if (bidAmount == null || taxRate == null) return;
        if (taxAmount != null && amountWithoutTax != null) {
            BigDecimal expectedTotal = taxAmount.add(amountWithoutTax);
            if (bidAmount.compareTo(expectedTotal) != 0) {
                throw new BusinessException("含税金额应等于税额加不含税金额");
            }
        }
    }

    private void validatePaymentBatchRatio(List<ProjectCreateDTO.PaymentBatchDTO> batches) {
        BigDecimal totalRatio = batches.stream()
                .map(ProjectCreateDTO.PaymentBatchDTO::getRatio)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalRatio.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException("付款批次比例之和不能超过100%");
        }
    }

    private ProjectVO toVO(BizProject project) {
        ProjectVO vo = new ProjectVO();
        vo.setId(project.getId());
        vo.setProjectNo(project.getProjectNo());
        vo.setProjectName(project.getProjectName());
        vo.setProjectType(project.getProjectType());
        vo.setProjectTypeName(PROJECT_TYPE_MAP.getOrDefault(project.getProjectType(), "未知"));
        vo.setStatus(project.getStatus());
        vo.setStatusName(getStatusName(project.getStatus()));
        vo.setDescription(project.getDescription());
        vo.setOwnerId(project.getOwnerId());
        vo.setDeptId(project.getDeptId());
        vo.setBidAmount(project.getBidAmount());
        vo.setTaxRate(project.getTaxRate());
        vo.setTaxAmount(project.getTaxAmount());
        vo.setAmountWithoutTax(project.getAmountWithoutTax());
        vo.setInvestLimit(project.getInvestLimit());
        vo.setBidNoticeUrl(project.getBidNoticeUrl());
        vo.setTerminationReason(project.getTerminationReason());
        vo.setCostTargetProjectId(project.getCostTargetProjectId());
        vo.setApprovedAt(project.getApprovedAt());
        vo.setPausedAt(project.getPausedAt());
        vo.setClosedAt(project.getClosedAt());
        vo.setCreatedAt(project.getCreatedAt());

        if (project.getOwnerId() != null) {
            SysUser owner = userMapper.selectById(project.getOwnerId());
            if (owner != null) vo.setOwnerName(owner.getRealName());
        }
        if (project.getDeptId() != null) {
            SysDepartment dept = departmentMapper.selectById(project.getDeptId());
            if (dept != null) vo.setDeptName(dept.getDeptName());
        }
        vo.setPaymentBatches(Collections.emptyList());
        return vo;
    }

    private String getStatusName(int status) {
        for (ProjectStatus ps : ProjectStatus.values()) {
            if (ps.getValue() == status) return ps.getDesc();
        }
        return "未知";
    }
}
