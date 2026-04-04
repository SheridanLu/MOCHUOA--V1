package com.mochu.business.contact.service;

import com.mochu.business.contact.vo.ContactVO;

import java.util.List;

public interface BizContactService {
    List<ContactVO> listByDept(Long deptId, String keyword);
    List<ContactVO> search(String keyword);
    void syncFromUser(Long userId);
    void hideByUser(Long userId);
}
