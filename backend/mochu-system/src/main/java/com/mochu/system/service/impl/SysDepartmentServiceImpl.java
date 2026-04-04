package com.mochu.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mochu.common.exception.BusinessException;
import com.mochu.system.dto.DeptCreateDTO;
import com.mochu.system.entity.SysDepartment;
import com.mochu.system.entity.SysUser;
import com.mochu.system.mapper.SysDepartmentMapper;
import com.mochu.system.mapper.SysUserMapper;
import com.mochu.system.service.SysDepartmentService;
import com.mochu.system.vo.DeptTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysDepartmentServiceImpl implements SysDepartmentService {

    private final SysDepartmentMapper departmentMapper;
    private final SysUserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public List<DeptTreeVO> getDeptTree() {
        List<SysDepartment> all = departmentMapper.selectList(
                new LambdaQueryWrapper<SysDepartment>().orderByAsc(SysDepartment::getSortOrder));
        List<DeptTreeVO> voList = all.stream().map(this::toVO).collect(Collectors.toList());
        return buildTree(voList);
    }

    @Override
    @Transactional
    public Long createDept(DeptCreateDTO dto) {
        SysDepartment dept = new SysDepartment();
        dept.setDeptName(dto.getDeptName());
        dept.setParentId(dto.getParentId());
        dept.setSortOrder(dto.getSortOrder());
        dept.setLeaderId(dto.getLeaderId());
        dept.setStatus(1);
        if (dto.getParentId() != 0) {
            SysDepartment parent = departmentMapper.selectById(dto.getParentId());
            if (parent == null) throw new BusinessException("上级部门不存在");
            departmentMapper.insert(dept);
            dept.setPath(parent.getPath() + "/" + dept.getId());
        } else {
            departmentMapper.insert(dept);
            dept.setPath("/" + dept.getId());
        }
        departmentMapper.updateById(dept);
        return dept.getId();
    }

    @Override
    @Transactional
    public void updateDept(Long id, DeptCreateDTO dto) {
        SysDepartment dept = departmentMapper.selectById(id);
        if (dept == null) throw new BusinessException(404, "部门不存在");
        dept.setDeptName(dto.getDeptName());
        dept.setSortOrder(dto.getSortOrder());
        dept.setLeaderId(dto.getLeaderId());
        departmentMapper.updateById(dept);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        SysDepartment dept = departmentMapper.selectById(id);
        if (dept == null) throw new BusinessException(404, "部门不存在");
        if (status == 0) {
            long count = userMapper.selectCount(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, id).eq(SysUser::getStatus, 1));
            if (count > 0) throw new BusinessException("该部门下有在职员工，不可停用");
        }
        dept.setStatus(status);
        departmentMapper.updateById(dept);
    }

    private DeptTreeVO toVO(SysDepartment dept) {
        DeptTreeVO vo = new DeptTreeVO();
        vo.setId(dept.getId());
        vo.setDeptName(dept.getDeptName());
        vo.setParentId(dept.getParentId());
        vo.setPath(dept.getPath());
        vo.setSortOrder(dept.getSortOrder());
        vo.setStatus(dept.getStatus());
        vo.setLeaderId(dept.getLeaderId());
        vo.setChildren(new ArrayList<>());
        return vo;
    }

    private List<DeptTreeVO> buildTree(List<DeptTreeVO> list) {
        Map<Long, DeptTreeVO> map = new LinkedHashMap<>();
        list.forEach(v -> map.put(v.getId(), v));
        List<DeptTreeVO> roots = new ArrayList<>();
        for (DeptTreeVO v : list) {
            if (v.getParentId() == 0) roots.add(v);
            else { DeptTreeVO p = map.get(v.getParentId()); if (p != null) p.getChildren().add(v); }
        }
        return roots;
    }
}
