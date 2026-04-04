package com.mochu.business.material.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.business.contract.entity.BizContract;
import com.mochu.business.contract.entity.BizContractItem;
import com.mochu.business.contract.mapper.BizContractItemMapper;
import com.mochu.business.contract.mapper.BizContractMapper;
import com.mochu.business.material.dto.InboundCreateDTO;
import com.mochu.business.material.dto.MaterialQueryDTO;
import com.mochu.business.material.dto.OutboundCreateDTO;
import com.mochu.business.material.dto.ReturnCreateDTO;
import com.mochu.business.material.entity.*;
import com.mochu.business.material.mapper.*;
import com.mochu.business.material.service.BizMaterialService;
import com.mochu.business.material.vo.*;
import com.mochu.business.project.entity.BizProject;
import com.mochu.business.project.mapper.BizProjectMapper;
import com.mochu.business.supplier.entity.BizSupplier;
import com.mochu.business.supplier.mapper.BizSupplierMapper;
import com.mochu.common.constant.Constants;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizMaterialServiceImpl implements BizMaterialService {

    private final BizMaterialInboundMapper inboundMapper;
    private final BizMaterialInboundItemMapper inboundItemMapper;
    private final BizMaterialOutboundMapper outboundMapper;
    private final BizMaterialOutboundItemMapper outboundItemMapper;
    private final BizMaterialReturnMapper returnMapper;
    private final BizMaterialReturnItemMapper returnItemMapper;
    private final BizInventoryMapper inventoryMapper;
    private final BizProjectMapper projectMapper;
    private final BizSupplierMapper supplierMapper;
    private final BizContractMapper contractMapper;
    private final BizContractItemMapper contractItemMapper;
    private final BizNoGenerator bizNoGenerator;

    private static final Map<Integer, String> INBOUND_STATUS = Map.of(1, "草稿", 2, "待审批", 3, "财务已审", 4, "已入库");
    private static final Map<Integer, String> OUTBOUND_STATUS = Map.of(1, "草稿", 2, "待审批", 3, "采购员已审", 4, "财务已审", 5, "已出库");
    private static final Map<Integer, String> RETURN_TYPE_MAP = Map.of(1, "现场处置", 2, "退回厂家", 3, "入公司仓库", 4, "项目间调拨");

    // ==================== INBOUND ====================

    @Override
    @Transactional(readOnly = true)
    public PageResult<InboundVO> listInbounds(MaterialQueryDTO query) {
        int page = query.getPage() != null ? query.getPage() : Constants.DEFAULT_PAGE;
        int size = Math.min(query.getSize() != null ? query.getSize() : Constants.DEFAULT_SIZE, Constants.MAX_SIZE);

        LambdaQueryWrapper<BizMaterialInbound> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(BizMaterialInbound::getInboundNo, query.getKeyword());
        }
        if (query.getProjectId() != null) wrapper.eq(BizMaterialInbound::getProjectId, query.getProjectId());
        if (query.getStatus() != null) wrapper.eq(BizMaterialInbound::getStatus, query.getStatus());
        wrapper.orderByDesc(BizMaterialInbound::getCreatedAt);

        IPage<BizMaterialInbound> pageResult = inboundMapper.selectPage(new Page<>(page, size), wrapper);
        List<InboundVO> voList = pageResult.getRecords().stream().map(this::toInboundVO).collect(Collectors.toList());
        return new PageResult<>(voList, pageResult.getTotal(), page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public InboundVO getInboundById(Long id) {
        BizMaterialInbound inbound = inboundMapper.selectById(id);
        if (inbound == null) throw new BusinessException(404, "入库单不存在");
        InboundVO vo = toInboundVO(inbound);
        LambdaQueryWrapper<BizMaterialInboundItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(BizMaterialInboundItem::getInboundId, id);
        vo.setItems(inboundItemMapper.selectList(itemWrapper).stream().map(this::toInboundItemVO).collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional
    public Long createInbound(InboundCreateDTO dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException("入库明细不能为空");
        }

        // Validate quantities against contract remaining
        if (dto.getContractId() != null) {
            for (InboundCreateDTO.InboundItemDTO item : dto.getItems()) {
                if (item.getContractItemId() != null) {
                    validateInboundQuantity(item.getContractItemId(), item.getQuantity());
                }
            }
        }

        BizMaterialInbound inbound = new BizMaterialInbound();
        inbound.setInboundNo(bizNoGenerator.generateInboundNo());
        inbound.setContractId(dto.getContractId());
        inbound.setProjectId(dto.getProjectId());
        inbound.setSupplierId(dto.getSupplierId());
        inbound.setWarehouse(dto.getWarehouse());
        inbound.setReceiver(dto.getReceiver());
        inbound.setInboundDate(LocalDate.now());
        inbound.setStatus(1);
        inbound.setCreatorId(SecurityUtils.getCurrentUserId());

        BigDecimal total = BigDecimal.ZERO;
        for (InboundCreateDTO.InboundItemDTO item : dto.getItems()) {
            BigDecimal amount = item.getQuantity().multiply(item.getUnitPrice()).setScale(2, RoundingMode.HALF_UP);
            total = total.add(amount);
        }
        inbound.setTotalAmount(total);
        inboundMapper.insert(inbound);

        for (InboundCreateDTO.InboundItemDTO itemDTO : dto.getItems()) {
            BizMaterialInboundItem item = new BizMaterialInboundItem();
            item.setInboundId(inbound.getId());
            item.setMaterialName(itemDTO.getMaterialName());
            item.setSpec(itemDTO.getSpec());
            item.setUnit(itemDTO.getUnit());
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(itemDTO.getUnitPrice());
            item.setAmount(itemDTO.getQuantity().multiply(itemDTO.getUnitPrice()).setScale(2, RoundingMode.HALF_UP));
            item.setContractItemId(itemDTO.getContractItemId());
            item.setRemark(itemDTO.getRemark());
            inboundItemMapper.insert(item);
        }

        log.info("Inbound created: {}", inbound.getInboundNo());
        return inbound.getId();
    }

    @Override
    @Transactional
    public void submitInbound(Long id) {
        BizMaterialInbound inbound = inboundMapper.selectById(id);
        if (inbound == null) throw new BusinessException(404, "入库单不存在");
        if (inbound.getStatus() != 1) throw new BusinessException("只有草稿状态可以提交");
        inbound.setStatus(2);
        inboundMapper.updateById(inbound);
    }

    @Override
    @Transactional
    public void approveInbound(Long id, String comment) {
        BizMaterialInbound inbound = inboundMapper.selectById(id);
        if (inbound == null) throw new BusinessException(404, "入库单不存在");
        if (comment == null || comment.length() < 2) throw new BusinessException("审批意见至少2个字符");

        if (inbound.getStatus() == 2) {
            // Purchaser -> Finance
            inbound.setStatus(3);
            inboundMapper.updateById(inbound);
        } else if (inbound.getStatus() == 3) {
            // Finance approved -> inventory updated
            inbound.setStatus(4);
            inboundMapper.updateById(inbound);
            updateInventoryOnInbound(inbound);
            log.info("Inbound {} approved, inventory updated", inbound.getInboundNo());
        } else {
            throw new BusinessException("当前状态不支持审批");
        }
    }

    @Override
    @Transactional
    public void rejectInbound(Long id, String comment) {
        BizMaterialInbound inbound = inboundMapper.selectById(id);
        if (inbound == null) throw new BusinessException(404, "入库单不存在");
        if (inbound.getStatus() != 2 && inbound.getStatus() != 3) throw new BusinessException("当前状态不支持驳回");
        if (comment == null || comment.length() < 5) throw new BusinessException("驳回意见至少5个字符");
        inbound.setStatus(1);
        inboundMapper.updateById(inbound);
    }

    @Override
    @Transactional
    public void autoCreateInboundDraft(Long contractId) {
        BizContract contract = contractMapper.selectById(contractId);
        if (contract == null || contract.getContractType() != 2) return;

        LambdaQueryWrapper<BizContractItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(BizContractItem::getContractId, contractId);
        List<BizContractItem> contractItems = contractItemMapper.selectList(itemWrapper);
        if (contractItems.isEmpty()) return;

        BizMaterialInbound inbound = new BizMaterialInbound();
        inbound.setInboundNo(bizNoGenerator.generateInboundNo());
        inbound.setContractId(contractId);
        inbound.setProjectId(contract.getProjectId());
        inbound.setSupplierId(contract.getSupplierId());
        inbound.setInboundDate(LocalDate.now());
        inbound.setStatus(1);
        inbound.setCreatorId(contract.getCreatorId());

        BigDecimal total = BigDecimal.ZERO;
        inboundMapper.insert(inbound);

        for (BizContractItem ci : contractItems) {
            BizMaterialInboundItem item = new BizMaterialInboundItem();
            item.setInboundId(inbound.getId());
            item.setMaterialName(ci.getMaterialName());
            item.setSpec(ci.getSpec());
            item.setUnit(ci.getUnit());
            item.setQuantity(ci.getQuantity());
            item.setUnitPrice(ci.getUnitPrice());
            item.setAmount(ci.getAmount());
            item.setContractItemId(ci.getId());
            inboundItemMapper.insert(item);
            if (ci.getAmount() != null) total = total.add(ci.getAmount());
        }

        inbound.setTotalAmount(total);
        inboundMapper.updateById(inbound);
        log.info("Auto-created inbound draft {} from contract {}", inbound.getInboundNo(), contract.getContractNo());
    }

    // ==================== OUTBOUND ====================

    @Override
    @Transactional(readOnly = true)
    public PageResult<OutboundVO> listOutbounds(MaterialQueryDTO query) {
        int page = query.getPage() != null ? query.getPage() : Constants.DEFAULT_PAGE;
        int size = Math.min(query.getSize() != null ? query.getSize() : Constants.DEFAULT_SIZE, Constants.MAX_SIZE);

        LambdaQueryWrapper<BizMaterialOutbound> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(BizMaterialOutbound::getOutboundNo, query.getKeyword());
        }
        if (query.getProjectId() != null) wrapper.eq(BizMaterialOutbound::getProjectId, query.getProjectId());
        if (query.getStatus() != null) wrapper.eq(BizMaterialOutbound::getStatus, query.getStatus());
        wrapper.orderByDesc(BizMaterialOutbound::getCreatedAt);

        IPage<BizMaterialOutbound> pageResult = outboundMapper.selectPage(new Page<>(page, size), wrapper);
        List<OutboundVO> voList = pageResult.getRecords().stream().map(this::toOutboundVO).collect(Collectors.toList());
        return new PageResult<>(voList, pageResult.getTotal(), page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public OutboundVO getOutboundById(Long id) {
        BizMaterialOutbound outbound = outboundMapper.selectById(id);
        if (outbound == null) throw new BusinessException(404, "出库单不存在");
        OutboundVO vo = toOutboundVO(outbound);
        LambdaQueryWrapper<BizMaterialOutboundItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(BizMaterialOutboundItem::getOutboundId, id);
        vo.setItems(outboundItemMapper.selectList(itemWrapper).stream().map(this::toOutboundItemVO).collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional
    public Long createOutbound(OutboundCreateDTO dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException("出库明细不能为空");
        }

        // Validate inventory
        for (OutboundCreateDTO.OutboundItemDTO item : dto.getItems()) {
            BizInventory inv = getInventoryRecord(dto.getProjectId(), item.getMaterialName(), item.getSpec(), dto.getWarehouse());
            if (inv == null || inv.getQuantity().compareTo(item.getQuantity()) < 0) {
                throw new BusinessException("物资 " + item.getMaterialName() + " 库存不足");
            }
        }

        BizMaterialOutbound outbound = new BizMaterialOutbound();
        outbound.setOutboundNo(bizNoGenerator.generateOutboundNo());
        outbound.setProjectId(dto.getProjectId());
        outbound.setWarehouse(dto.getWarehouse());
        outbound.setRecipient(dto.getRecipient());
        outbound.setPurpose(dto.getPurpose());
        outbound.setOutboundDate(LocalDate.now());
        outbound.setStatus(1);
        outbound.setCreatorId(SecurityUtils.getCurrentUserId());
        outboundMapper.insert(outbound);

        for (OutboundCreateDTO.OutboundItemDTO itemDTO : dto.getItems()) {
            BizInventory inv = getInventoryRecord(dto.getProjectId(), itemDTO.getMaterialName(), itemDTO.getSpec(), dto.getWarehouse());
            BizMaterialOutboundItem item = new BizMaterialOutboundItem();
            item.setOutboundId(outbound.getId());
            item.setMaterialName(itemDTO.getMaterialName());
            item.setSpec(itemDTO.getSpec());
            item.setUnit(itemDTO.getUnit());
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(inv.getWeightedAvgPrice());
            item.setAmount(itemDTO.getQuantity().multiply(inv.getWeightedAvgPrice()).setScale(2, RoundingMode.HALF_UP));
            item.setRemark(itemDTO.getRemark());
            outboundItemMapper.insert(item);
        }

        log.info("Outbound created: {}", outbound.getOutboundNo());
        return outbound.getId();
    }

    @Override
    @Transactional
    public void submitOutbound(Long id) {
        BizMaterialOutbound outbound = outboundMapper.selectById(id);
        if (outbound == null) throw new BusinessException(404, "出库单不存在");
        if (outbound.getStatus() != 1) throw new BusinessException("只有草稿状态可以提交");
        outbound.setStatus(2);
        outboundMapper.updateById(outbound);
    }

    @Override
    @Transactional
    public void approveOutbound(Long id, String comment) {
        BizMaterialOutbound outbound = outboundMapper.selectById(id);
        if (outbound == null) throw new BusinessException(404, "出库单不存在");
        if (comment == null || comment.length() < 2) throw new BusinessException("审批意见至少2个字符");

        if (outbound.getStatus() == 2) {
            outbound.setStatus(3); // PM -> Purchaser
            outboundMapper.updateById(outbound);
        } else if (outbound.getStatus() == 3) {
            outbound.setStatus(4); // Purchaser -> Finance
            outboundMapper.updateById(outbound);
        } else if (outbound.getStatus() == 4) {
            // Finance -> GM -> approved, deduct inventory
            outbound.setStatus(5);
            outboundMapper.updateById(outbound);
            deductInventoryOnOutbound(outbound);
            log.info("Outbound {} approved, inventory deducted", outbound.getOutboundNo());
        } else {
            throw new BusinessException("当前状态不支持审批");
        }
    }

    @Override
    @Transactional
    public void rejectOutbound(Long id, String comment) {
        BizMaterialOutbound outbound = outboundMapper.selectById(id);
        if (outbound == null) throw new BusinessException(404, "出库单不存在");
        if (outbound.getStatus() < 2 || outbound.getStatus() > 4) throw new BusinessException("当前状态不支持驳回");
        if (comment == null || comment.length() < 5) throw new BusinessException("驳回意见至少5个字符");
        outbound.setStatus(1);
        outboundMapper.updateById(outbound);
    }

    // ==================== RETURN ====================

    @Override
    @Transactional(readOnly = true)
    public PageResult<ReturnVO> listReturns(MaterialQueryDTO query) {
        int page = query.getPage() != null ? query.getPage() : Constants.DEFAULT_PAGE;
        int size = Math.min(query.getSize() != null ? query.getSize() : Constants.DEFAULT_SIZE, Constants.MAX_SIZE);

        LambdaQueryWrapper<BizMaterialReturn> wrapper = new LambdaQueryWrapper<>();
        if (query.getProjectId() != null) wrapper.eq(BizMaterialReturn::getProjectId, query.getProjectId());
        if (query.getStatus() != null) wrapper.eq(BizMaterialReturn::getStatus, query.getStatus());
        wrapper.orderByDesc(BizMaterialReturn::getCreatedAt);

        IPage<BizMaterialReturn> pageResult = returnMapper.selectPage(new Page<>(page, size), wrapper);
        List<ReturnVO> voList = pageResult.getRecords().stream().map(this::toReturnVO).collect(Collectors.toList());
        return new PageResult<>(voList, pageResult.getTotal(), page, size);
    }

    @Override
    @Transactional
    public Long createReturn(ReturnCreateDTO dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException("退料明细不能为空");
        }
        if (dto.getReturnType() == null || dto.getReturnType() < 1 || dto.getReturnType() > 4) {
            throw new BusinessException("退料类型无效");
        }
        if (dto.getReturnType() == 4 && dto.getTargetProjectId() == null) {
            throw new BusinessException("项目间调拨必须指定目标项目");
        }

        BizMaterialReturn ret = new BizMaterialReturn();
        ret.setReturnNo(bizNoGenerator.generateReturnNo());
        ret.setProjectId(dto.getProjectId());
        ret.setReturnDate(LocalDate.now());
        ret.setReason(dto.getReason());
        ret.setStatus(1);
        ret.setReturnType(dto.getReturnType());
        ret.setTargetProjectId(dto.getTargetProjectId());
        ret.setCreatorId(SecurityUtils.getCurrentUserId());
        returnMapper.insert(ret);

        for (ReturnCreateDTO.ReturnItemDTO itemDTO : dto.getItems()) {
            BizMaterialReturnItem item = new BizMaterialReturnItem();
            item.setReturnId(ret.getId());
            item.setInboundItemId(itemDTO.getInboundItemId());
            item.setMaterialName(itemDTO.getMaterialName());
            item.setSpec(itemDTO.getSpec());
            item.setUnit(itemDTO.getUnit());
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(itemDTO.getUnitPrice());
            item.setAmount(itemDTO.getQuantity().multiply(itemDTO.getUnitPrice()).setScale(2, RoundingMode.HALF_UP));
            returnItemMapper.insert(item);
        }

        log.info("Return created: {} type={}", ret.getReturnNo(), dto.getReturnType());
        return ret.getId();
    }

    @Override
    @Transactional
    public void approveReturn(Long id, String comment) {
        BizMaterialReturn ret = returnMapper.selectById(id);
        if (ret == null) throw new BusinessException(404, "退料单不存在");
        if (ret.getStatus() != 1) throw new BusinessException("当前状态不支持审批");
        if (comment == null || comment.length() < 2) throw new BusinessException("审批意见至少2个字符");

        ret.setStatus(3);
        returnMapper.updateById(ret);

        LambdaQueryWrapper<BizMaterialReturnItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(BizMaterialReturnItem::getReturnId, id);
        List<BizMaterialReturnItem> items = returnItemMapper.selectList(itemWrapper);

        for (BizMaterialReturnItem item : items) {
            // Deduct from source project inventory
            deductInventory(ret.getProjectId(), item.getMaterialName(), item.getSpec(), "", item.getQuantity());

            switch (ret.getReturnType()) {
                case 1: // On-site disposal - deducted, counted as wastage cost
                    break;
                case 2: // Return to manufacturer - deducted, not counted as cost
                    break;
                case 3: // Into company warehouse (project_id=0)
                    addInventory(0L, item.getMaterialName(), item.getSpec(), item.getUnit(), "公司仓库", item.getQuantity(), item.getUnitPrice());
                    break;
                case 4: // Inter-project transfer
                    addInventory(ret.getTargetProjectId(), item.getMaterialName(), item.getSpec(), item.getUnit(), "", item.getQuantity(), item.getUnitPrice());
                    break;
            }
        }
        log.info("Return {} approved, inventory adjusted (type={})", ret.getReturnNo(), ret.getReturnType());
    }

    // ==================== INVENTORY ====================

    @Override
    @Transactional(readOnly = true)
    public List<InventoryVO> queryInventory(Long projectId, String keyword, String warehouse) {
        LambdaQueryWrapper<BizInventory> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) wrapper.eq(BizInventory::getProjectId, projectId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(BizInventory::getMaterialName, keyword)
                    .or().like(BizInventory::getSpec, keyword));
        }
        if (StringUtils.hasText(warehouse)) wrapper.eq(BizInventory::getWarehouse, warehouse);
        wrapper.gt(BizInventory::getQuantity, 0);
        wrapper.orderByAsc(BizInventory::getMaterialName);

        return inventoryMapper.selectList(wrapper).stream().map(inv -> {
            InventoryVO vo = new InventoryVO();
            vo.setId(inv.getId());
            vo.setProjectId(inv.getProjectId());
            vo.setMaterialName(inv.getMaterialName());
            vo.setSpec(inv.getSpec());
            vo.setUnit(inv.getUnit());
            vo.setQuantity(inv.getQuantity());
            vo.setWeightedAvgPrice(inv.getWeightedAvgPrice());
            vo.setTotalAmount(inv.getTotalAmount());
            vo.setWarehouse(inv.getWarehouse());
            if (inv.getProjectId() != null && inv.getProjectId() > 0) {
                BizProject project = projectMapper.selectById(inv.getProjectId());
                if (project != null) vo.setProjectName(project.getProjectName());
            } else {
                vo.setProjectName("公司仓库");
            }
            return vo;
        }).collect(Collectors.toList());
    }

    // ==================== INVENTORY HELPERS ====================

    private void updateInventoryOnInbound(BizMaterialInbound inbound) {
        LambdaQueryWrapper<BizMaterialInboundItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(BizMaterialInboundItem::getInboundId, inbound.getId());
        List<BizMaterialInboundItem> items = inboundItemMapper.selectList(itemWrapper);

        for (BizMaterialInboundItem item : items) {
            addInventory(inbound.getProjectId(), item.getMaterialName(), item.getSpec(),
                    item.getUnit(), inbound.getWarehouse() != null ? inbound.getWarehouse() : "",
                    item.getQuantity(), item.getUnitPrice());
        }
    }

    private void addInventory(Long projectId, String materialName, String spec, String unit, String warehouse,
                               BigDecimal quantity, BigDecimal unitPrice) {
        BizInventory inv = getInventoryRecord(projectId, materialName, spec, warehouse);
        if (inv != null) {
            // Weighted average price calculation
            BigDecimal oldTotal = inv.getQuantity().multiply(inv.getWeightedAvgPrice() != null ? inv.getWeightedAvgPrice() : BigDecimal.ZERO);
            BigDecimal newTotal = quantity.multiply(unitPrice != null ? unitPrice : BigDecimal.ZERO);
            BigDecimal totalQty = inv.getQuantity().add(quantity);
            BigDecimal newAvgPrice = totalQty.compareTo(BigDecimal.ZERO) > 0
                    ? oldTotal.add(newTotal).divide(totalQty, 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            inv.setQuantity(totalQty);
            inv.setWeightedAvgPrice(newAvgPrice);
            inv.setTotalAmount(totalQty.multiply(newAvgPrice).setScale(2, RoundingMode.HALF_UP));
            inventoryMapper.updateById(inv);
        } else {
            inv = new BizInventory();
            inv.setProjectId(projectId);
            inv.setMaterialName(materialName);
            inv.setSpec(spec != null ? spec : "");
            inv.setUnit(unit);
            inv.setWarehouse(warehouse != null ? warehouse : "");
            inv.setQuantity(quantity);
            inv.setWeightedAvgPrice(unitPrice != null ? unitPrice : BigDecimal.ZERO);
            inv.setTotalAmount(quantity.multiply(unitPrice != null ? unitPrice : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
            inventoryMapper.insert(inv);
        }
    }

    private void deductInventory(Long projectId, String materialName, String spec, String warehouse, BigDecimal quantity) {
        BizInventory inv = getInventoryRecord(projectId, materialName, spec, warehouse);
        if (inv == null || inv.getQuantity().compareTo(quantity) < 0) {
            throw new BusinessException("物资 " + materialName + " 库存不足");
        }
        inv.setQuantity(inv.getQuantity().subtract(quantity));
        inv.setTotalAmount(inv.getQuantity().multiply(inv.getWeightedAvgPrice()).setScale(2, RoundingMode.HALF_UP));
        inventoryMapper.updateById(inv);
    }

    private void deductInventoryOnOutbound(BizMaterialOutbound outbound) {
        LambdaQueryWrapper<BizMaterialOutboundItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(BizMaterialOutboundItem::getOutboundId, outbound.getId());
        List<BizMaterialOutboundItem> items = outboundItemMapper.selectList(itemWrapper);

        for (BizMaterialOutboundItem item : items) {
            deductInventory(outbound.getProjectId(), item.getMaterialName(), item.getSpec(),
                    outbound.getWarehouse() != null ? outbound.getWarehouse() : "", item.getQuantity());
        }
    }

    private BizInventory getInventoryRecord(Long projectId, String materialName, String spec, String warehouse) {
        LambdaQueryWrapper<BizInventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizInventory::getProjectId, projectId);
        wrapper.eq(BizInventory::getMaterialName, materialName);
        wrapper.eq(BizInventory::getSpec, spec != null ? spec : "");
        wrapper.eq(BizInventory::getWarehouse, warehouse != null ? warehouse : "");
        return inventoryMapper.selectOne(wrapper);
    }

    private void validateInboundQuantity(Long contractItemId, BigDecimal inboundQty) {
        BizContractItem contractItem = contractItemMapper.selectById(contractItemId);
        if (contractItem == null) return;

        // Sum already inbound quantities for this contract item
        LambdaQueryWrapper<BizMaterialInboundItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizMaterialInboundItem::getContractItemId, contractItemId);
        List<BizMaterialInboundItem> existing = inboundItemMapper.selectList(wrapper);
        BigDecimal alreadyInbound = existing.stream()
                .map(BizMaterialInboundItem::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = contractItem.getQuantity().subtract(alreadyInbound);
        if (inboundQty.compareTo(remaining) > 0) {
            throw new BusinessException("入库数量(" + inboundQty + ")超过合同剩余数量(" + remaining + ")");
        }
    }

    // ==================== VO CONVERTERS ====================

    private InboundVO toInboundVO(BizMaterialInbound inbound) {
        InboundVO vo = new InboundVO();
        vo.setId(inbound.getId());
        vo.setInboundNo(inbound.getInboundNo());
        vo.setContractId(inbound.getContractId());
        vo.setProjectId(inbound.getProjectId());
        vo.setSupplierId(inbound.getSupplierId());
        vo.setWarehouse(inbound.getWarehouse());
        vo.setReceiver(inbound.getReceiver());
        vo.setInboundDate(inbound.getInboundDate());
        vo.setStatus(inbound.getStatus());
        vo.setStatusName(INBOUND_STATUS.getOrDefault(inbound.getStatus(), "未知"));
        vo.setTotalAmount(inbound.getTotalAmount());
        vo.setCreatedAt(inbound.getCreatedAt());
        if (inbound.getProjectId() != null) {
            BizProject p = projectMapper.selectById(inbound.getProjectId());
            if (p != null) vo.setProjectName(p.getProjectName());
        }
        if (inbound.getSupplierId() != null) {
            BizSupplier s = supplierMapper.selectById(inbound.getSupplierId());
            if (s != null) vo.setSupplierName(s.getSupplierName());
        }
        vo.setItems(Collections.emptyList());
        return vo;
    }

    private InboundVO.InboundItemVO toInboundItemVO(BizMaterialInboundItem item) {
        InboundVO.InboundItemVO vo = new InboundVO.InboundItemVO();
        vo.setId(item.getId());
        vo.setMaterialName(item.getMaterialName());
        vo.setSpec(item.getSpec());
        vo.setUnit(item.getUnit());
        vo.setQuantity(item.getQuantity());
        vo.setUnitPrice(item.getUnitPrice());
        vo.setAmount(item.getAmount());
        vo.setContractItemId(item.getContractItemId());
        vo.setRemark(item.getRemark());
        return vo;
    }

    private OutboundVO toOutboundVO(BizMaterialOutbound outbound) {
        OutboundVO vo = new OutboundVO();
        vo.setId(outbound.getId());
        vo.setOutboundNo(outbound.getOutboundNo());
        vo.setProjectId(outbound.getProjectId());
        vo.setWarehouse(outbound.getWarehouse());
        vo.setRecipient(outbound.getRecipient());
        vo.setPurpose(outbound.getPurpose());
        vo.setOutboundDate(outbound.getOutboundDate());
        vo.setStatus(outbound.getStatus());
        vo.setStatusName(OUTBOUND_STATUS.getOrDefault(outbound.getStatus(), "未知"));
        vo.setCreatedAt(outbound.getCreatedAt());
        if (outbound.getProjectId() != null) {
            BizProject p = projectMapper.selectById(outbound.getProjectId());
            if (p != null) vo.setProjectName(p.getProjectName());
        }
        vo.setItems(Collections.emptyList());
        return vo;
    }

    private OutboundVO.OutboundItemVO toOutboundItemVO(BizMaterialOutboundItem item) {
        OutboundVO.OutboundItemVO vo = new OutboundVO.OutboundItemVO();
        vo.setId(item.getId());
        vo.setMaterialName(item.getMaterialName());
        vo.setSpec(item.getSpec());
        vo.setUnit(item.getUnit());
        vo.setQuantity(item.getQuantity());
        vo.setUnitPrice(item.getUnitPrice());
        vo.setAmount(item.getAmount());
        vo.setRemark(item.getRemark());
        return vo;
    }

    private ReturnVO toReturnVO(BizMaterialReturn ret) {
        ReturnVO vo = new ReturnVO();
        vo.setId(ret.getId());
        vo.setReturnNo(ret.getReturnNo());
        vo.setInboundId(ret.getInboundId());
        vo.setProjectId(ret.getProjectId());
        vo.setReturnDate(ret.getReturnDate());
        vo.setReason(ret.getReason());
        vo.setStatus(ret.getStatus());
        vo.setStatusName(ret.getStatus() == 1 ? "待审批" : ret.getStatus() == 3 ? "已审批" : "未知");
        vo.setReturnType(ret.getReturnType());
        vo.setReturnTypeName(RETURN_TYPE_MAP.getOrDefault(ret.getReturnType(), "未知"));
        vo.setTargetProjectId(ret.getTargetProjectId());
        vo.setCreatedAt(ret.getCreatedAt());
        if (ret.getProjectId() != null) {
            BizProject p = projectMapper.selectById(ret.getProjectId());
            if (p != null) vo.setProjectName(p.getProjectName());
        }
        if (ret.getTargetProjectId() != null) {
            BizProject tp = projectMapper.selectById(ret.getTargetProjectId());
            if (tp != null) vo.setTargetProjectName(tp.getProjectName());
        }
        vo.setItems(Collections.emptyList());
        return vo;
    }
}
