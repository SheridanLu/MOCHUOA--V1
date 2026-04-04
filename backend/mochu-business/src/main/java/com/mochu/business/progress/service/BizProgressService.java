package com.mochu.business.progress.service;

import com.mochu.business.progress.dto.*;
import com.mochu.business.progress.vo.*;

import java.util.List;

public interface BizProgressService {
    List<PlanVO> listPlans(Long projectId);
    Long createPlan(PlanCreateDTO dto);
    void approvePlan(Long id, String comment);
    List<TaskVO> getGanttTasks(Long planId);
    Long createTask(TaskCreateDTO dto);
    void updateProgress(ProgressUpdateDTO dto);
    void batchUpdateProgress(ProgressUpdateDTO dto);
    List<DeviationVO> listDeviations(Long projectId);
    void scanDeviations(Long projectId);
}
