package com.mochu.business.contract.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.business.contract.dto.ContractCreateDTO;
import com.mochu.business.contract.dto.ContractQueryDTO;
import com.mochu.business.contract.dto.SupplementCreateDTO;
import com.mochu.business.contract.entity.BizContract;
import com.mochu.business.contract.entity.BizContractItem;
import com.mochu.business.contract.entity.BizContractSupplement;
import com.mochu.business.contract.mapper.BizContractItemMapper;
import com.mochu.business.contract.mapper.BizContractMapper;
import com.mochu.business.contract.mapper.BizContractSupplementMapper;
import com.mochu.business.contract.vo.ContractVO;
import com.mochu.business.project.entity.BizProject;
import com.mochu.business.project.mapper.BizProjectMapper;
import com.mochu.business.purchase.entity.BizPurchaseList;
import com.mochu.business.purchase.mapper.BizPurchaseListMapper;
import com.mochu.business.supplier.entity.BizSupplier;
import com.mochu.business.supplier.mapper.BizSupplierMapper;
import com.mochu.common.constant.Constants;
import com.mochu.common.enums.ContractStatus;
import com.mochu.common.exception.BusinessException;
import com.mochu.common.result.PageResult;
import com.mochu.common.utils.BizNoGenerator;
import com.mochu.common.utils.SecurityUtils;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizContractServiceImpl implements BizContractService {

    private final BizContractMapper contractMapper;
    private final BizContractItemMapper itemMapper;
    private final BizContractSupplementMapper supplementMapper;
    private final BizProjectMapper projectMapper;
    private final BizSupplierMapper supplierMapper;
    private final BizNoGenerator bizNoGenerator;

    private static final BigDecimal PRICE_WARNING_YELLOW = new BigDecimal("0.01");
    private static final BigDecimal PRICE_WARNING_RED = new BigDecimal("0.01");

    @Override
    @Transactional(readOnly = true)
    public PageResult<ContractVO> listContracts(ContractQueryDTO query) {
        int page = query.getPage() != null ? query.getPage() : Constants.DEFAULT_PAGE;
        int size = Math.min(query.getSize() != null ? query.getSize() : Constants.DEFAULT_SIZE, Constants.MAX_SIZE);

        LambdaQueryWrapper<BizContract> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(BizContract::getContractName, query.getKeyword())
                    .or().like(BizContract::getContractNo, query.getKeyword()));
        }
        if (query.getContractType() != null) wrapper.eq(BizContract::getContractType, query.getContractType());
        if (query.getStatus() != null) wrapper.eq(BizContract::getStatus, query.getStatus());
        if (query.getProjectId() != null) wrapper.eq(BizContract::getProjectId, query.getProjectId());
        if (query.getSupplierId() != null) wrapper.eq(BizContract::getSupplierId, query.getSupplierId());
        wrapper.orderByDesc(BizContract::getCreatedAt);

        IPage<BizContract> pageResult = contractMapper.selectPage(new Page<>(page, size), wrapper);
        List<ContractVO> voList = pageResult.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(voList, pageResult.getTotal(), page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractVO getContractById(Long id) {
        BizContract contract = contractMapper.selectById(id);
        if (contract == null) throw new BusinessException(404, "合同不存在");
        ContractVO vo = toVO(contract);

        // Load items
        LambdaQueryWrapper<BizContractItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(BizContractItem::getContractId, id);
        vo.setItems(itemMapper.selectList(itemWrapper).stream().map(item -> {
            ContractVO.ContractItemVO iv = new ContractVO.ContractItemVO();
            iv.setId(item.getId());
            iv.setMaterialName(item.getMaterialName());
            iv.setSpec(item.getSpec());
            iv.setUnit(item.getUnit());
            iv.setQuantity(item.getQuantity());
            iv.setUnitPrice(item.getUnitPrice());
            iv.setAmount(item.getAmount());
            iv.setRemark(item.getRemark());
            return iv;
        }).collect(Collectors.toList()));

        // Load supplements
        LambdaQueryWrapper<BizContractSupplement> supWrapper = new LambdaQueryWrapper<>();
        supWrapper.eq(BizContractSupplement::getContractId, id).orderByAsc(BizContractSupplement::getCreatedAt);
        vo.setSupplements(supplementMapper.selectList(supWrapper).stream().map(s -> {
            ContractVO.SupplementVO sv = new ContractVO.SupplementVO();
            sv.setId(s.getId());
            sv.setSupplementNo(s.getSupplementNo());
            sv.setReason(s.getReason());
            sv.setAmountChange(s.getAmountChange());
            sv.setNewTotal(s.getNewTotal());
            sv.setStatus(s.getStatus());
            sv.setCreatedAt(s.getCreatedAt());
            return sv;
        }).collect(Collectors.toList()));

        return vo;
    }

    @Override
    @Transactional
    public Long createContract(ContractCreateDTO dto) {
        BizProject project = projectMapper.selectById(dto.getProjectId());
        if (project == null) throw new BusinessException("所属项目不存在");

        // Validate items for expenditure contracts (type=2)
        if (dto.getContractType() == 2 && dto.getItems() != null) {
            for (ContractCreateDTO.ContractItemDTO item : dto.getItems()) {
                if (!StringUtils.hasText(item.getMaterialName())) {
                    throw new BusinessException("支出合同物资名称不能为空");
                }
                if (!StringUtils.hasText(item.getUnit())) {
                    throw new BusinessException("支出合同物资单位不能为空");
                }
                if (item.getQuantity() == null) {
                    throw new BusinessException("支出合同物资数量不能为空");
                }
            }
        }

        BizContract contract = new BizContract();
        contract.setContractName(dto.getContractName());
        contract.setContractType(dto.getContractType());
        contract.setProjectId(dto.getProjectId());
        contract.setSupplierId(dto.getSupplierId());
        contract.setAmountWithTax(dto.getAmountWithTax());
        contract.setTaxRate(dto.getTaxRate());
        contract.setTaxAmount(dto.getTaxAmount());
        contract.setAmountWithoutTax(dto.getAmountWithoutTax());
        if (dto.getSignDate() != null) contract.setSignDate(LocalDate.parse(dto.getSignDate()));
        if (dto.getStartDate() != null) contract.setStartDate(LocalDate.parse(dto.getStartDate()));
        if (dto.getEndDate() != null) contract.setEndDate(LocalDate.parse(dto.getEndDate()));
        contract.setTemplateId(dto.getTemplateId());
        contract.setRemark(dto.getRemark());
        contract.setCreatorId(SecurityUtils.getCurrentUserId());
        contract.setStatus(ContractStatus.DRAFT.getValue());

        // Generate contract number
        if (dto.getContractType() == 1) {
            contract.setContractNo(bizNoGenerator.generateIncomeContractNo());
        } else {
            contract.setContractNo(bizNoGenerator.generateExpenditureContractNo());
        }

        contractMapper.insert(contract);

        // Save items
        if (dto.getItems() != null) {
            for (ContractCreateDTO.ContractItemDTO itemDTO : dto.getItems()) {
                BizContractItem item = new BizContractItem();
                item.setContractId(contract.getId());
                item.setMaterialName(itemDTO.getMaterialName());
                item.setSpec(itemDTO.getSpec());
                item.setUnit(itemDTO.getUnit());
                item.setQuantity(itemDTO.getQuantity());
                item.setUnitPrice(itemDTO.getUnitPrice());
                item.setAmount(itemDTO.getAmount());
                item.setRemark(itemDTO.getRemark());
                itemMapper.insert(item);
            }
        }

        log.info("Contract created: {} ({})", contract.getContractNo(), contract.getContractName());
        return contract.getId();
    }

    @Override
    @Transactional
    public void submitForApproval(Long id) {
        BizContract contract = contractMapper.selectById(id);
        if (contract == null) throw new BusinessException(404, "合同不存在");
        if (contract.getStatus() != ContractStatus.DRAFT.getValue()) {
            throw new BusinessException("只有草稿状态的合同可以提交审批");
        }

        // Check overrun for expenditure contracts (skip for virtual projects)
        if (contract.getContractType() == 2) {
            BizProject project = projectMapper.selectById(contract.getProjectId());
            if (project != null && project.getProjectType() != 2) {
                checkOverrun(contract);
            }
        }

        contract.setStatus(ContractStatus.PENDING.getValue());
        contractMapper.updateById(contract);
        log.info("Contract {} submitted for approval", contract.getContractNo());
    }

    @Override
    @Transactional
    public void approve(Long id, String comment) {
        BizContract contract = contractMapper.selectById(id);
        if (contract == null) throw new BusinessException(404, "合同不存在");
        int s = contract.getStatus();
        if (s != ContractStatus.PENDING.getValue() && s != ContractStatus.FIN_APPROVED.getValue() && s != ContractStatus.LEGAL_APPROVED.getValue()) {
            throw new BusinessException("当前状态不允许审批");
        }
        if (comment == null || comment.length() < 2) {
            throw new BusinessException("审批意见至少2个字符");
        }

        // Advance through approval chain
        if (s == ContractStatus.PENDING.getValue()) {
            contract.setStatus(ContractStatus.FIN_APPROVED.getValue());
        } else if (s == ContractStatus.FIN_APPROVED.getValue()) {
            contract.setStatus(ContractStatus.LEGAL_APPROVED.getValue());
        } else {
            contract.setStatus(ContractStatus.APPROVED.getValue());
        }
        contractMapper.updateById(contract);
        log.info("Contract {} approved to status {}", contract.getContractNo(), contract.getStatus());
    }

    @Override
    @Transactional
    public void reject(Long id, String comment) {
        BizContract contract = contractMapper.selectById(id);
        if (contract == null) throw new BusinessException(404, "合同不存在");
        if (comment == null || comment.length() < 5) {
            throw new BusinessException("驳回意见至少5个字符");
        }
        contract.setStatus(ContractStatus.DRAFT.getValue());
        contractMapper.updateById(contract);
        log.info("Contract {} rejected: {}", contract.getContractNo(), comment);
    }

    @Override
    @Transactional
    public void terminate(Long id, String reason) {
        BizContract contract = contractMapper.selectById(id);
        if (contract == null) throw new BusinessException(404, "合同不存在");
        if (contract.getStatus() < ContractStatus.APPROVED.getValue()) {
            throw new BusinessException("只有已审批或执行中的合同可以终止");
        }
        contract.setStatus(ContractStatus.TERMINATED.getValue());
        contractMapper.updateById(contract);
        log.info("Contract {} terminated: {}", contract.getContractNo(), reason);
    }

    @Override
    @Transactional
    public Long createSupplement(Long contractId, SupplementCreateDTO dto) {
        BizContract contract = contractMapper.selectById(contractId);
        if (contract == null) throw new BusinessException(404, "合同不存在");
        if (contract.getStatus() < ContractStatus.APPROVED.getValue()) {
            throw new BusinessException("只有已审批的合同才能签订补充协议");
        }

        BizContractSupplement supplement = new BizContractSupplement();
        supplement.setContractId(contractId);
        supplement.setSupplementNo(bizNoGenerator.generateSupplementNo());
        supplement.setReason(dto.getReason());
        supplement.setAmountChange(dto.getAmountChange());
        supplement.setNewTotal(dto.getNewTotal());
        supplement.setFileUrl(dto.getFileUrl());
        supplement.setStatus(ContractStatus.DRAFT.getValue());
        supplementMapper.insert(supplement);

        log.info("Supplement {} created for contract {}", supplement.getSupplementNo(), contract.getContractNo());
        return supplement.getId();
    }

    private void checkOverrun(BizContract contract) {
        // Check items against purchase list planned quantities
        LambdaQueryWrapper<BizContractItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(BizContractItem::getContractId, contract.getId());
        List<BizContractItem> items = itemMapper.selectList(itemWrapper);

        for (BizContractItem item : items) {
            if (item.getUnitPrice() != null && item.getQuantity() != null) {
                // Price warning logic: check against baseline price
                // Yellow warning: <1% above baseline, Red warning: >1% above baseline
                // This is a placeholder for actual baseline price lookup
                log.debug("Price check for item {} in contract {}", item.getMaterialName(), contract.getContractNo());
            }
        }
    }

    private ContractVO toVO(BizContract contract) {
        ContractVO vo = new ContractVO();
        vo.setId(contract.getId());
        vo.setContractNo(contract.getContractNo());
        vo.setContractName(contract.getContractName());
        vo.setContractType(contract.getContractType());
        vo.setContractTypeName(contract.getContractType() == 1 ? "收入合同" : "支出合同");
        vo.setProjectId(contract.getProjectId());
        vo.setSupplierId(contract.getSupplierId());
        vo.setAmountWithTax(contract.getAmountWithTax());
        vo.setTaxRate(contract.getTaxRate());
        vo.setTaxAmount(contract.getTaxAmount());
        vo.setAmountWithoutTax(contract.getAmountWithoutTax());
        vo.setSignDate(contract.getSignDate());
        vo.setStartDate(contract.getStartDate());
        vo.setEndDate(contract.getEndDate());
        vo.setStatus(contract.getStatus());
        vo.setStatusName(getStatusName(contract.getStatus()));
        vo.setRemark(contract.getRemark());
        vo.setCreatedAt(contract.getCreatedAt());

        if (contract.getProjectId() != null) {
            BizProject project = projectMapper.selectById(contract.getProjectId());
            if (project != null) vo.setProjectName(project.getProjectName());
        }
        if (contract.getSupplierId() != null) {
            BizSupplier supplier = supplierMapper.selectById(contract.getSupplierId());
            if (supplier != null) vo.setSupplierName(supplier.getSupplierName());
        }
        vo.setItems(Collections.emptyList());
        vo.setSupplements(Collections.emptyList());
        return vo;
    }

    private String getStatusName(int status) {
        for (ContractStatus cs : ContractStatus.values()) {
            if (cs.getValue() == status) return cs.getDesc();
        }
        return "未知";
    }
}
