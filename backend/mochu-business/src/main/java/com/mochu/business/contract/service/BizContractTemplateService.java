package com.mochu.business.contract.service;

import com.mochu.business.contract.dto.TemplateCreateDTO;
import com.mochu.business.contract.vo.TemplateVO;

import java.util.List;

public interface BizContractTemplateService {
    List<TemplateVO> listTemplates(Integer templateType, Integer status);
    TemplateVO getTemplateById(Long id);
    Long createTemplate(TemplateCreateDTO dto);
    void updateTemplate(Long id, TemplateCreateDTO dto);
    void submitForApproval(Long id);
    void approve(Long id, String comment);
    void reject(Long id, String comment);
    String renderTemplate(Long id);
}
