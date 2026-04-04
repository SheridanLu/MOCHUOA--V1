package com.mochu.system.service;

import com.mochu.system.dto.DeptCreateDTO;
import com.mochu.system.vo.DeptTreeVO;
import java.util.List;

public interface SysDepartmentService {
    List<DeptTreeVO> getDeptTree();
    Long createDept(DeptCreateDTO dto);
    void updateDept(Long id, DeptCreateDTO dto);
    void updateStatus(Long id, Integer status);
}
