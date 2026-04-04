package com.mochu.business.contact.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mochu.business.contact.entity.BizContact;
import com.mochu.business.contact.mapper.BizContactMapper;
import com.mochu.business.contact.service.BizContactService;
import com.mochu.business.contact.vo.ContactVO;
import com.mochu.common.utils.LoginUser;
import com.mochu.common.utils.SecurityUtils;
import com.mochu.system.entity.SysDepartment;
import com.mochu.system.entity.SysUser;
import com.mochu.system.mapper.SysDepartmentMapper;
import com.mochu.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BizContactServiceImpl implements BizContactService {

    private final BizContactMapper contactMapper;
    private final SysUserMapper userMapper;
    private final SysDepartmentMapper departmentMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ContactVO> listByDept(Long deptId, String keyword) {
        LambdaQueryWrapper<BizContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizContact::getVisible, 1);
        if (deptId != null) wrapper.eq(BizContact::getDeptId, deptId);
        if (StringUtils.hasText(keyword) && keyword.length() >= 2) {
            wrapper.and(w -> w.like(BizContact::getRealName, keyword)
                    .or().like(BizContact::getPhone, keyword)
                    .or().like(BizContact::getPosition, keyword));
        }
        wrapper.orderByAsc(BizContact::getRealName);

        List<BizContact> contacts = contactMapper.selectList(wrapper);
        boolean canSeeFullPhone = canViewFullPhone();
        Map<Long, String> deptNameMap = loadDeptNames(contacts);

        return contacts.stream().map(c -> {
            ContactVO vo = new ContactVO();
            vo.setId(c.getId());
            vo.setUserId(c.getUserId());
            vo.setDeptId(c.getDeptId());
            vo.setDeptName(deptNameMap.getOrDefault(c.getDeptId(), ""));
            vo.setRealName(c.getRealName());
            vo.setPhone(canSeeFullPhone ? c.getPhone() : maskPhone(c.getPhone()));
            vo.setEmail(c.getEmail());
            vo.setPosition(c.getPosition());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactVO> search(String keyword) {
        if (!StringUtils.hasText(keyword) || keyword.length() < 2) return Collections.emptyList();
        return listByDept(null, keyword);
    }

    @Override
    @Transactional
    public void syncFromUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) return;

        LambdaQueryWrapper<BizContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizContact::getUserId, userId);
        BizContact existing = contactMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setRealName(user.getRealName());
            existing.setPhone(user.getPhone());
            existing.setEmail(user.getEmail());
            existing.setDeptId(user.getDeptId());
            existing.setVisible(1);
            contactMapper.updateById(existing);
        } else {
            BizContact contact = new BizContact();
            contact.setUserId(userId);
            contact.setDeptId(user.getDeptId());
            contact.setRealName(user.getRealName());
            contact.setPhone(user.getPhone());
            contact.setEmail(user.getEmail());
            contact.setVisible(1);
            contactMapper.insert(contact);
        }
    }

    @Override
    @Transactional
    public void hideByUser(Long userId) {
        LambdaQueryWrapper<BizContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizContact::getUserId, userId);
        BizContact contact = contactMapper.selectOne(wrapper);
        if (contact != null) {
            contact.setVisible(0);
            contactMapper.updateById(contact);
        }
    }

    private boolean canViewFullPhone() {
        try {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser == null) return false;
            Set<String> perms = loginUser.getPermissions();
            return perms != null && (perms.contains("contact:view_phone") || perms.contains("hr:view") || perms.contains("*"));
        } catch (Exception e) {
            return false;
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private Map<Long, String> loadDeptNames(List<BizContact> contacts) {
        Set<Long> deptIds = contacts.stream().map(BizContact::getDeptId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (deptIds.isEmpty()) return Collections.emptyMap();
        return departmentMapper.selectBatchIds(deptIds).stream()
                .collect(Collectors.toMap(SysDepartment::getId, SysDepartment::getDeptName, (a, b) -> a));
    }
}
