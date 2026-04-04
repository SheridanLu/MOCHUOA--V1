package com.mochu.business.project.service;

import com.mochu.business.project.dto.ProjectCreateDTO;
import com.mochu.business.project.dto.ProjectQueryDTO;
import com.mochu.business.project.dto.ProjectUpdateDTO;
import com.mochu.business.project.vo.ProjectVO;
import com.mochu.common.result.PageResult;

public interface BizProjectService {
    PageResult<ProjectVO> listProjects(ProjectQueryDTO query);
    ProjectVO getProjectById(Long id);
    Long createProject(ProjectCreateDTO dto);
    void updateProject(Long id, ProjectUpdateDTO dto);
    void submitForApproval(Long id);
    void approve(Long id, String comment);
    void reject(Long id, String comment);
    void pause(Long id);
    void resume(Long id);
    void close(Long id);
    void terminate(Long id, String reason);
    void convertVirtualToEntity(Long virtualId, Long costTargetProjectId);
}
