package com.mochu.business.contract.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mochu.business.contract.dto.TemplateCreateDTO;
import com.mochu.business.contract.entity.BizContractTemplate;
import com.mochu.business.contract.mapper.BizContractTemplateMapper;
import com.mochu.business.contract.service.BizContractTemplateService;
import com.mochu.business.contract.vo.TemplateVO;
import com.mochu.common.exception.BusinessException;
import com.mochu.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizContractTemplateServiceImpl implements BizContractTemplateService {

    private final BizContractTemplateMapper templateMapper;

    private static final Map<Integer, String> TYPE_MAP = Map.of(
            1, "收入合同模板", 2, "支出合同模板"
    );
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    // Template status: 1=草稿, 2=待审批, 3=已审批
    private static final int STATUS_DRAFT = 1;
    private static final int STATUS_PENDING = 2;
    private static final int STATUS_APPROVED = 3;

    @Override
    @Transactional(readOnly = true)
    public List<TemplateVO> listTemplates(Integer templateType, Integer status) {
        LambdaQueryWrapper<BizContractTemplate> wrapper = new LambdaQueryWrapper<>();
        if (templateType != null) wrapper.eq(BizContractTemplate::getTemplateType, templateType);
        if (status != null) wrapper.eq(BizContractTemplate::getStatus, status);
        wrapper.orderByDesc(BizContractTemplate::getCreatedAt);
        return templateMapper.selectList(wrapper).stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TemplateVO getTemplateById(Long id) {
        BizContractTemplate template = templateMapper.selectById(id);
        if (template == null) throw new BusinessException(404, "模板不存在");
        return toVO(template);
    }

    @Override
    @Transactional
    public Long createTemplate(TemplateCreateDTO dto) {
        BizContractTemplate template = new BizContractTemplate();
        template.setTemplateName(dto.getTemplateName());
        template.setTemplateType(dto.getTemplateType());
        template.setContent(dto.getContent());
        template.setFileUrl(dto.getFileUrl());
        template.setVersion(1);
        template.setStatus(STATUS_DRAFT);
        template.setCreatorId(SecurityUtils.getCurrentUserId());
        templateMapper.insert(template);
        log.info("Template created: {}", template.getTemplateName());
        return template.getId();
    }

    @Override
    @Transactional
    public void updateTemplate(Long id, TemplateCreateDTO dto) {
        BizContractTemplate template = templateMapper.selectById(id);
        if (template == null) throw new BusinessException(404, "模板不存在");
        if (template.getStatus() != STATUS_DRAFT) throw new BusinessException("只有草稿状态的模板可以编辑");
        template.setTemplateName(dto.getTemplateName());
        template.setTemplateType(dto.getTemplateType());
        template.setContent(dto.getContent());
        template.setFileUrl(dto.getFileUrl());
        template.setVersion(template.getVersion() + 1);
        templateMapper.updateById(template);
    }

    @Override
    @Transactional
    public void submitForApproval(Long id) {
        BizContractTemplate template = templateMapper.selectById(id);
        if (template == null) throw new BusinessException(404, "模板不存在");
        if (template.getStatus() != STATUS_DRAFT) throw new BusinessException("只有草稿状态的模板可以提交审批");
        template.setStatus(STATUS_PENDING);
        templateMapper.updateById(template);
    }

    @Override
    @Transactional
    public void approve(Long id, String comment) {
        BizContractTemplate template = templateMapper.selectById(id);
        if (template == null) throw new BusinessException(404, "模板不存在");
        if (template.getStatus() != STATUS_PENDING) throw new BusinessException("只有待审批的模板可以审批");
        if (comment == null || comment.length() < 2) throw new BusinessException("审批意见至少2个字符");
        template.setStatus(STATUS_APPROVED);
        templateMapper.updateById(template);
    }

    @Override
    @Transactional
    public void reject(Long id, String comment) {
        BizContractTemplate template = templateMapper.selectById(id);
        if (template == null) throw new BusinessException(404, "模板不存在");
        if (template.getStatus() != STATUS_PENDING) throw new BusinessException("只有待审批的模板可以驳回");
        if (comment == null || comment.length() < 5) throw new BusinessException("驳回意见至少5个字符");
        template.setStatus(STATUS_DRAFT);
        templateMapper.updateById(template);
    }

    @Override
    @Transactional(readOnly = true)
    public String renderTemplate(Long id) {
        BizContractTemplate template = templateMapper.selectById(id);
        if (template == null) throw new BusinessException(404, "模板不存在");
        if (template.getContent() == null) return "";

        // Extract variable placeholders like ${companyName}, ${contractAmount}
        // Return content with variables replaced by input-field markers
        String content = template.getContent();
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String varName = matcher.group(1);
            matcher.appendReplacement(sb, "<input name=\"" + varName + "\" placeholder=\"" + varName + "\" />");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private TemplateVO toVO(BizContractTemplate template) {
        TemplateVO vo = new TemplateVO();
        vo.setId(template.getId());
        vo.setTemplateName(template.getTemplateName());
        vo.setTemplateType(template.getTemplateType());
        vo.setTemplateTypeName(TYPE_MAP.getOrDefault(template.getTemplateType(), "未知"));
        vo.setContent(template.getContent());
        vo.setFileUrl(template.getFileUrl());
        vo.setVersion(template.getVersion());
        vo.setStatus(template.getStatus());
        vo.setStatusName(template.getStatus() == STATUS_DRAFT ? "草稿" : template.getStatus() == STATUS_PENDING ? "待审批" : "已审批");
        vo.setCreatedAt(template.getCreatedAt());
        return vo;
    }
}
