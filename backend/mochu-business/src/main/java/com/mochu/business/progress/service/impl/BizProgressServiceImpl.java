package com.mochu.business.progress.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mochu.business.progress.dto.PlanCreateDTO;
import com.mochu.business.progress.dto.ProgressUpdateDTO;
import com.mochu.business.progress.dto.TaskCreateDTO;
import com.mochu.business.progress.entity.BizProgressDeviation;
import com.mochu.business.progress.entity.BizProgressPlan;
import com.mochu.business.progress.entity.BizProgressTask;
import com.mochu.business.progress.mapper.BizProgressDeviationMapper;
import com.mochu.business.progress.mapper.BizProgressPlanMapper;
import com.mochu.business.progress.mapper.BizProgressTaskMapper;
import com.mochu.business.progress.service.BizProgressService;
import com.mochu.business.progress.vo.DeviationVO;
import com.mochu.business.progress.vo.PlanVO;
import com.mochu.business.progress.vo.TaskVO;
import com.mochu.business.project.entity.BizProject;
import com.mochu.business.project.mapper.BizProjectMapper;
import com.mochu.common.exception.BusinessException;
import com.mochu.common.utils.SecurityUtils;
import com.mochu.system.entity.SysUser;
import com.mochu.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizProgressServiceImpl implements BizProgressService {

    private final BizProgressPlanMapper planMapper;
    private final BizProgressTaskMapper taskMapper;
    private final BizProgressDeviationMapper deviationMapper;
    private final BizProjectMapper projectMapper;
    private final SysUserMapper userMapper;

    /** Plan status: 1=draft, 2=pending, 3=approved(locked) */
    private static final int PLAN_DRAFT = 1;
    private static final int PLAN_PENDING = 2;
    private static final int PLAN_APPROVED = 3;

    private static final Map<Integer, String> PLAN_STATUS_MAP = Map.of(
            PLAN_DRAFT, "草稿",
            PLAN_PENDING, "待审批",
            PLAN_APPROVED, "已审批"
    );

    /** Task status: 1=未开始, 2=进行中, 3=已完成, 4=已延期, 5=暂停 */
    private static final int TASK_NOT_STARTED = 1;
    private static final int TASK_IN_PROGRESS = 2;
    private static final int TASK_COMPLETED = 3;
    private static final int TASK_DELAYED = 4;
    private static final int TASK_PAUSED = 5;

    private static final Map<Integer, String> TASK_STATUS_MAP = Map.of(
            TASK_NOT_STARTED, "未开始",
            TASK_IN_PROGRESS, "进行中",
            TASK_COMPLETED, "已完成",
            TASK_DELAYED, "已延期",
            TASK_PAUSED, "暂停"
    );

    /** Deviation type: 1=schedule delay, 2=progress lag */
    private static final Map<Integer, String> DEVIATION_TYPE_MAP = Map.of(
            1, "进度延期",
            2, "进度滞后"
    );

    // ========== Plan ==========

    @Override
    @Transactional(readOnly = true)
    public List<PlanVO> listPlans(Long projectId) {
        LambdaQueryWrapper<BizProgressPlan> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(BizProgressPlan::getProjectId, projectId);
        }
        wrapper.orderByDesc(BizProgressPlan::getCreatedAt);
        List<BizProgressPlan> plans = planMapper.selectList(wrapper);
        return plans.stream().map(this::toPlanVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Long createPlan(PlanCreateDTO dto) {
        BizProject project = projectMapper.selectById(dto.getProjectId());
        if (project == null) {
            throw new BusinessException(404, "项目不存在");
        }

        BizProgressPlan plan = new BizProgressPlan();
        plan.setProjectId(dto.getProjectId());
        plan.setPlanName(dto.getPlanName());
        plan.setStartDate(dto.getStartDate());
        plan.setEndDate(dto.getEndDate());
        plan.setStatus(PLAN_DRAFT);
        plan.setCreatorId(SecurityUtils.getCurrentUserId());
        planMapper.insert(plan);

        log.info("Progress plan created: {} for project {}", plan.getPlanName(), dto.getProjectId());
        return plan.getId();
    }

    @Override
    @Transactional
    public void approvePlan(Long id, String comment) {
        BizProgressPlan plan = planMapper.selectById(id);
        if (plan == null) {
            throw new BusinessException(404, "计划不存在");
        }
        if (plan.getStatus() == PLAN_APPROVED) {
            throw new BusinessException("计划已审批，不能重复审批");
        }
        if (comment == null || comment.length() < 2) {
            throw new BusinessException("审批意见至少2个字符");
        }
        plan.setStatus(PLAN_APPROVED);
        planMapper.updateById(plan);
        log.info("Progress plan {} approved", plan.getPlanName());
    }

    // ========== Task / Gantt ==========

    @Override
    @Transactional(readOnly = true)
    public List<TaskVO> getGanttTasks(Long planId) {
        LambdaQueryWrapper<BizProgressTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizProgressTask::getPlanId, planId)
                .orderByAsc(BizProgressTask::getSortOrder)
                .orderByAsc(BizProgressTask::getId);
        List<BizProgressTask> allTasks = taskMapper.selectList(wrapper);

        // Collect assignee IDs for batch lookup
        Set<Long> assigneeIds = allTasks.stream()
                .map(BizProgressTask::getAssigneeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> userNameMap = new HashMap<>();
        if (!assigneeIds.isEmpty()) {
            for (Long userId : assigneeIds) {
                SysUser user = userMapper.selectById(userId);
                if (user != null) {
                    userNameMap.put(userId, user.getRealName());
                }
            }
        }

        // Convert to VOs
        List<TaskVO> voList = allTasks.stream()
                .map(task -> toTaskVO(task, userNameMap))
                .collect(Collectors.toList());

        // Build tree structure
        return buildTaskTree(voList);
    }

    @Override
    @Transactional
    public Long createTask(TaskCreateDTO dto) {
        BizProgressPlan plan = planMapper.selectById(dto.getPlanId());
        if (plan == null) {
            throw new BusinessException(404, "计划不存在");
        }
        if (plan.getStatus() == PLAN_APPROVED) {
            throw new BusinessException("已审批的计划不能添加任务");
        }

        // Validate parent task if provided
        if (dto.getParentTaskId() != null) {
            BizProgressTask parent = taskMapper.selectById(dto.getParentTaskId());
            if (parent == null) {
                throw new BusinessException(404, "父任务不存在");
            }
            if (!parent.getPlanId().equals(dto.getPlanId())) {
                throw new BusinessException("父任务不属于当前计划");
            }
        }

        BizProgressTask task = new BizProgressTask();
        task.setPlanId(dto.getPlanId());
        task.setProjectId(dto.getProjectId());
        task.setTaskName(dto.getTaskName());
        task.setParentTaskId(dto.getParentTaskId());
        task.setStartDate(dto.getStartDate());
        task.setEndDate(dto.getEndDate());
        task.setAssigneeId(dto.getAssigneeId());
        task.setMilestone(dto.getMilestone());
        task.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        task.setProgress(BigDecimal.ZERO);
        task.setStatus(TASK_NOT_STARTED);
        taskMapper.insert(task);

        log.info("Progress task created: {} in plan {}", task.getTaskName(), dto.getPlanId());
        return task.getId();
    }

    // ========== Progress Update ==========

    @Override
    @Transactional
    public void updateProgress(ProgressUpdateDTO dto) {
        if (dto.getTaskId() == null) {
            throw new BusinessException("任务ID不能为空");
        }
        BizProgressTask task = taskMapper.selectById(dto.getTaskId());
        if (task == null) {
            throw new BusinessException(404, "任务不存在");
        }

        // Update progress fields
        if (dto.getProgress() != null) {
            task.setProgress(dto.getProgress());
        }
        if (dto.getActualStart() != null) {
            task.setActualStart(dto.getActualStart());
        }
        if (dto.getActualEnd() != null) {
            task.setActualEnd(dto.getActualEnd());
        }

        // Auto-determine task status
        task.setStatus(determineTaskStatus(task));
        taskMapper.updateById(task);

        // Save description to deviation record for immutability
        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            BizProgressDeviation deviation = new BizProgressDeviation();
            deviation.setTaskId(task.getId());
            deviation.setProjectId(task.getProjectId());
            deviation.setDeviationType(2); // progress update record
            deviation.setDeviationDays(0);
            deviation.setDescription(dto.getDescription());
            deviation.setCreatedAt(LocalDateTime.now());
            deviationMapper.insert(deviation);
        }

        log.info("Task {} progress updated to {}%", task.getTaskName(), task.getProgress());
    }

    @Override
    @Transactional
    public void batchUpdateProgress(ProgressUpdateDTO dto) {
        if (dto.getBatchItems() == null || dto.getBatchItems().isEmpty()) {
            throw new BusinessException("批量更新列表不能为空");
        }

        for (ProgressUpdateDTO.BatchItem item : dto.getBatchItems()) {
            BizProgressTask task = taskMapper.selectById(item.getTaskId());
            if (task == null) {
                throw new BusinessException(404, "任务不存在: ID=" + item.getTaskId());
            }

            if (item.getProgress() != null) {
                task.setProgress(item.getProgress());
            }

            // Auto-determine task status
            task.setStatus(determineTaskStatus(task));
            taskMapper.updateById(task);

            // Save description to deviation record for immutability
            if (item.getDescription() != null && !item.getDescription().isBlank()) {
                BizProgressDeviation deviation = new BizProgressDeviation();
                deviation.setTaskId(task.getId());
                deviation.setProjectId(task.getProjectId());
                deviation.setDeviationType(2);
                deviation.setDeviationDays(0);
                deviation.setDescription(item.getDescription());
                deviation.setCreatedAt(LocalDateTime.now());
                deviationMapper.insert(deviation);
            }
        }

        log.info("Batch progress update completed for {} tasks", dto.getBatchItems().size());
    }

    // ========== Deviation ==========

    @Override
    @Transactional(readOnly = true)
    public List<DeviationVO> listDeviations(Long projectId) {
        LambdaQueryWrapper<BizProgressDeviation> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(BizProgressDeviation::getProjectId, projectId);
        }
        wrapper.orderByDesc(BizProgressDeviation::getCreatedAt);
        List<BizProgressDeviation> deviations = deviationMapper.selectList(wrapper);

        // Collect task IDs for name lookup
        Set<Long> taskIds = deviations.stream()
                .map(BizProgressDeviation::getTaskId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> taskNameMap = new HashMap<>();
        if (!taskIds.isEmpty()) {
            for (Long taskId : taskIds) {
                BizProgressTask task = taskMapper.selectById(taskId);
                if (task != null) {
                    taskNameMap.put(taskId, task.getTaskName());
                }
            }
        }

        return deviations.stream().map(d -> {
            DeviationVO vo = new DeviationVO();
            vo.setId(d.getId());
            vo.setTaskId(d.getTaskId());
            vo.setTaskName(taskNameMap.getOrDefault(d.getTaskId(), ""));
            vo.setProjectId(d.getProjectId());
            vo.setDeviationDays(d.getDeviationDays());
            vo.setDeviationType(d.getDeviationType());
            vo.setDeviationTypeName(DEVIATION_TYPE_MAP.getOrDefault(d.getDeviationType(), "未知"));
            vo.setDescription(d.getDescription());
            vo.setCreatedAt(d.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void scanDeviations(Long projectId) {
        if (projectId == null) {
            throw new BusinessException("项目ID不能为空");
        }

        // Find all tasks where today > endDate and progress < 100
        LambdaQueryWrapper<BizProgressTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizProgressTask::getProjectId, projectId)
                .lt(BizProgressTask::getEndDate, LocalDate.now())
                .lt(BizProgressTask::getProgress, new BigDecimal("100"));
        List<BizProgressTask> delayedTasks = taskMapper.selectList(wrapper);

        int created = 0;
        for (BizProgressTask task : delayedTasks) {
            // Calculate deviation days
            int deviationDays = (int) ChronoUnit.DAYS.between(task.getEndDate(), LocalDate.now());

            // Check if deviation already recorded today for this task
            LambdaQueryWrapper<BizProgressDeviation> existWrapper = new LambdaQueryWrapper<>();
            existWrapper.eq(BizProgressDeviation::getTaskId, task.getId())
                    .eq(BizProgressDeviation::getDeviationType, 1)
                    .ge(BizProgressDeviation::getCreatedAt, LocalDate.now().atStartOfDay());
            Long existCount = deviationMapper.selectCount(existWrapper);
            if (existCount > 0) {
                continue;
            }

            BizProgressDeviation deviation = new BizProgressDeviation();
            deviation.setTaskId(task.getId());
            deviation.setProjectId(task.getProjectId());
            deviation.setDeviationType(1); // schedule delay
            deviation.setDeviationDays(deviationDays);
            deviation.setDescription("任务\"" + task.getTaskName() + "\"已延期" + deviationDays + "天，当前进度" + task.getProgress() + "%");
            deviation.setCreatedAt(LocalDateTime.now());
            deviationMapper.insert(deviation);

            // Update task status to DELAYED
            task.setStatus(TASK_DELAYED);
            taskMapper.updateById(task);
            created++;
        }

        log.info("Deviation scan completed for project {}: {} deviations created", projectId, created);
    }

    // ========== Private Helpers ==========

    /**
     * Auto-determine task status based on progress and dates.
     * 1=未开始, 2=进行中, 3=已完成, 4=已延期, 5=暂停
     */
    private int determineTaskStatus(BizProgressTask task) {
        // Preserve PAUSED status (manual set only)
        if (task.getStatus() != null && task.getStatus() == TASK_PAUSED) {
            return TASK_PAUSED;
        }

        BigDecimal progress = task.getProgress() != null ? task.getProgress() : BigDecimal.ZERO;
        LocalDate now = LocalDate.now();

        // COMPLETED: progress = 100
        if (progress.compareTo(new BigDecimal("100")) >= 0) {
            return TASK_COMPLETED;
        }

        // DELAYED: now > endDate and progress < 100
        if (task.getEndDate() != null && now.isAfter(task.getEndDate()) && progress.compareTo(new BigDecimal("100")) < 0) {
            return TASK_DELAYED;
        }

        // IN_PROGRESS: 0 < progress < 100
        if (progress.compareTo(BigDecimal.ZERO) > 0) {
            return TASK_IN_PROGRESS;
        }

        // NOT_STARTED: progress = 0 and now < startDate (or no startDate set)
        return TASK_NOT_STARTED;
    }

    private PlanVO toPlanVO(BizProgressPlan plan) {
        PlanVO vo = new PlanVO();
        vo.setId(plan.getId());
        vo.setProjectId(plan.getProjectId());
        vo.setPlanName(plan.getPlanName());
        vo.setStartDate(plan.getStartDate());
        vo.setEndDate(plan.getEndDate());
        vo.setStatus(plan.getStatus());
        vo.setStatusName(PLAN_STATUS_MAP.getOrDefault(plan.getStatus(), "未知"));
        vo.setCreatedAt(plan.getCreatedAt());

        // Resolve project name
        if (plan.getProjectId() != null) {
            BizProject project = projectMapper.selectById(plan.getProjectId());
            if (project != null) {
                vo.setProjectName(project.getProjectName());
            }
        }
        return vo;
    }

    private TaskVO toTaskVO(BizProgressTask task, Map<Long, String> userNameMap) {
        TaskVO vo = new TaskVO();
        vo.setId(task.getId());
        vo.setPlanId(task.getPlanId());
        vo.setProjectId(task.getProjectId());
        vo.setTaskName(task.getTaskName());
        vo.setParentTaskId(task.getParentTaskId());
        vo.setStartDate(task.getStartDate());
        vo.setEndDate(task.getEndDate());
        vo.setActualStart(task.getActualStart());
        vo.setActualEnd(task.getActualEnd());
        vo.setProgress(task.getProgress());
        vo.setStatus(task.getStatus());
        vo.setStatusName(TASK_STATUS_MAP.getOrDefault(task.getStatus(), "未知"));
        vo.setAssigneeId(task.getAssigneeId());
        vo.setAssigneeName(task.getAssigneeId() != null ? userNameMap.getOrDefault(task.getAssigneeId(), "") : null);
        vo.setMilestone(task.getMilestone());
        vo.setSortOrder(task.getSortOrder());
        vo.setChildren(new ArrayList<>());
        return vo;
    }

    /**
     * Build tree structure from flat task list using parentTaskId relationships.
     */
    private List<TaskVO> buildTaskTree(List<TaskVO> allTasks) {
        Map<Long, TaskVO> taskMap = new LinkedHashMap<>();
        for (TaskVO task : allTasks) {
            taskMap.put(task.getId(), task);
        }

        List<TaskVO> roots = new ArrayList<>();
        for (TaskVO task : allTasks) {
            if (task.getParentTaskId() == null) {
                roots.add(task);
            } else {
                TaskVO parent = taskMap.get(task.getParentTaskId());
                if (parent != null) {
                    parent.getChildren().add(task);
                } else {
                    // Orphan task: add to root level
                    roots.add(task);
                }
            }
        }
        return roots;
    }
}
