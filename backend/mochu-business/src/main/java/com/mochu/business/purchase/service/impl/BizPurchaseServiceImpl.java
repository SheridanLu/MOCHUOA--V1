package com.mochu.business.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.business.contract.entity.BizContract;
import com.mochu.business.contract.entity.BizContractItem;
import com.mochu.business.contract.mapper.BizContractItemMapper;
import com.mochu.business.contract.mapper.BizContractMapper;
import com.mochu.business.project.entity.BizProject;
import com.mochu.business.project.mapper.BizProjectMapper;
import com.mochu.business.purchase.dto.BenchmarkPriceDTO;
import com.mochu.business.purchase.dto.PurchaseListCreateDTO;
import com.mochu.business.purchase.dto.PurchaseListQueryDTO;
import com.mochu.business.purchase.entity.BizBenchmarkPrice;
import com.mochu.business.purchase.entity.BizPurchaseItem;
import com.mochu.business.purchase.entity.BizPurchaseList;
import com.mochu.business.purchase.mapper.BizBenchmarkPriceMapper;
import com.mochu.business.purchase.mapper.BizPurchaseItemMapper;
import com.mochu.business.purchase.mapper.BizPurchaseListMapper;
import com.mochu.business.purchase.service.BizPurchaseService;
import com.mochu.business.purchase.vo.BenchmarkPriceVO;
import com.mochu.business.purchase.vo.PurchaseListVO;
import com.mochu.common.constant.Constants;
import com.mochu.common.exception.BusinessException;
import com.mochu.common.result.PageResult;
import com.mochu.common.utils.BizNoGenerator;
import com.mochu.common.utils.SecurityUtils;
import com.mochu.system.entity.SysUser;
import com.mochu.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizPurchaseServiceImpl implements BizPurchaseService {

    private final BizPurchaseListMapper purchaseListMapper;
    private final BizPurchaseItemMapper purchaseItemMapper;
    private final BizBenchmarkPriceMapper benchmarkPriceMapper;
    private final BizProjectMapper projectMapper;
    private final BizContractMapper contractMapper;
    private final BizContractItemMapper contractItemMapper;
    private final SysUserMapper userMapper;
    private final BizNoGenerator bizNoGenerator;

    private static final Map<Integer, String> STATUS_MAP = Map.of(
            1, "草稿", 2, "待审批", 3, "财务已审", 4, "已审批", 5, "已变更"
    );

    @Override
    @Transactional(readOnly = true)
    public PageResult<PurchaseListVO> listPurchaseLists(PurchaseListQueryDTO query) {
        int page = query.getPage() != null ? query.getPage() : Constants.DEFAULT_PAGE;
        int size = Math.min(query.getSize() != null ? query.getSize() : Constants.DEFAULT_SIZE, Constants.MAX_SIZE);

        LambdaQueryWrapper<BizPurchaseList> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(BizPurchaseList::getListNo, query.getKeyword());
        }
        if (query.getProjectId() != null) wrapper.eq(BizPurchaseList::getProjectId, query.getProjectId());
        if (query.getStatus() != null) wrapper.eq(BizPurchaseList::getStatus, query.getStatus());
        wrapper.orderByDesc(BizPurchaseList::getCreatedAt);

        IPage<BizPurchaseList> pageResult = purchaseListMapper.selectPage(new Page<>(page, size), wrapper);
        List<PurchaseListVO> voList = pageResult.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(voList, pageResult.getTotal(), page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseListVO getPurchaseListById(Long id) {
        BizPurchaseList pl = purchaseListMapper.selectById(id);
        if (pl == null) throw new BusinessException(404, "采购清单不存在");
        PurchaseListVO vo = toVO(pl);
        LambdaQueryWrapper<BizPurchaseItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(BizPurchaseItem::getListId, id);
        List<BizPurchaseItem> items = purchaseItemMapper.selectList(itemWrapper);
        vo.setItems(items.stream().map(this::toItemVO).collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional
    public Long createPurchaseList(PurchaseListCreateDTO dto) {
        // Validate project exists
        BizProject project = projectMapper.selectById(dto.getProjectId());
        if (project == null) throw new BusinessException(404, "项目不存在");

        // Feature 115: Only one valid purchase list per project
        LambdaQueryWrapper<BizPurchaseList> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(BizPurchaseList::getProjectId, dto.getProjectId())
                .in(BizPurchaseList::getStatus, 1, 2, 3, 4);
        Long existCount = purchaseListMapper.selectCount(existWrapper);
        if (existCount > 0) {
            throw new BusinessException("该项目已有有效采购清单，不能重复创建");
        }

        // Validate items
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException("采购清单明细不能为空");
        }
        for (PurchaseListCreateDTO.PurchaseItemDTO item : dto.getItems()) {
            if (!StringUtils.hasText(item.getMaterialName())) {
                throw new BusinessException("物资名称不能为空");
            }
            if (!StringUtils.hasText(item.getUnit())) {
                throw new BusinessException("单位不能为空");
            }
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("计划数量必须大于0");
            }
        }

        BizPurchaseList pl = new BizPurchaseList();
        pl.setProjectId(dto.getProjectId());
        pl.setContractId(dto.getContractId());
        pl.setListNo(bizNoGenerator.generatePurchaseListNo());
        pl.setStatus(1);
        pl.setCreatorId(SecurityUtils.getCurrentUserId());

        // Calculate total
        BigDecimal total = dto.getItems().stream()
                .filter(i -> i.getEstimatedPrice() != null && i.getQuantity() != null)
                .map(i -> i.getEstimatedPrice().multiply(i.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        pl.setTotalAmount(total);

        purchaseListMapper.insert(pl);

        // Save items
        for (PurchaseListCreateDTO.PurchaseItemDTO itemDTO : dto.getItems()) {
            BizPurchaseItem item = new BizPurchaseItem();
            item.setListId(pl.getId());
            item.setMaterialName(itemDTO.getMaterialName());
            item.setSpec(itemDTO.getSpec());
            item.setUnit(itemDTO.getUnit());
            item.setQuantity(itemDTO.getQuantity());
            item.setEstimatedPrice(itemDTO.getEstimatedPrice());
            item.setRemark(itemDTO.getRemark());
            purchaseItemMapper.insert(item);
        }

        log.info("Purchase list created: {} for project {}", pl.getListNo(), project.getProjectNo());
        return pl.getId();
    }

    @Override
    @Transactional
    public void submitForApproval(Long id) {
        BizPurchaseList pl = purchaseListMapper.selectById(id);
        if (pl == null) throw new BusinessException(404, "采购清单不存在");
        if (pl.getStatus() != 1) throw new BusinessException("只有草稿状态可以提交审批");
        pl.setStatus(2);
        purchaseListMapper.updateById(pl);
        log.info("Purchase list {} submitted for approval", pl.getListNo());
    }

    @Override
    @Transactional
    public void approve(Long id, String comment) {
        BizPurchaseList pl = purchaseListMapper.selectById(id);
        if (pl == null) throw new BusinessException(404, "采购清单不存在");
        if (comment == null || comment.length() < 2) throw new BusinessException("审批意见至少2个字符");

        if (pl.getStatus() == 2) {
            // Finance approval
            pl.setStatus(3);
            purchaseListMapper.updateById(pl);
            log.info("Purchase list {} finance approved", pl.getListNo());
        } else if (pl.getStatus() == 3) {
            // GM final approval
            pl.setStatus(4);
            pl.setApproverId(SecurityUtils.getCurrentUserId());
            pl.setApprovedAt(LocalDateTime.now());
            purchaseListMapper.updateById(pl);
            log.info("Purchase list {} GM approved", pl.getListNo());
        } else {
            throw new BusinessException("当前状态不支持审批");
        }
    }

    @Override
    @Transactional
    public void reject(Long id, String comment) {
        BizPurchaseList pl = purchaseListMapper.selectById(id);
        if (pl == null) throw new BusinessException(404, "采购清单不存在");
        if (pl.getStatus() != 2 && pl.getStatus() != 3) throw new BusinessException("当前状态不支持驳回");
        if (comment == null || comment.length() < 5) throw new BusinessException("驳回意见至少5个字符");
        pl.setStatus(1);
        purchaseListMapper.updateById(pl);
        log.info("Purchase list {} rejected: {}", pl.getListNo(), comment);
    }

    @Override
    @Transactional
    public void requestChange(Long id) {
        BizPurchaseList pl = purchaseListMapper.selectById(id);
        if (pl == null) throw new BusinessException(404, "采购清单不存在");
        if (pl.getStatus() != 4) throw new BusinessException("只有已审批的采购清单可以发起变更");
        pl.setStatus(5);
        purchaseListMapper.updateById(pl);
        log.info("Purchase list {} change requested", pl.getListNo());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BenchmarkPriceVO> listBenchmarkPrices(String keyword) {
        LambdaQueryWrapper<BizBenchmarkPrice> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(BizBenchmarkPrice::getMaterialName, keyword)
                    .or().like(BizBenchmarkPrice::getSpec, keyword);
        }
        wrapper.eq(BizBenchmarkPrice::getStatus, 1);
        wrapper.orderByAsc(BizBenchmarkPrice::getMaterialName);
        return benchmarkPriceMapper.selectList(wrapper).stream().map(this::toBenchmarkVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Long createOrUpdateBenchmarkPrice(BenchmarkPriceDTO dto) {
        // Check if exists
        LambdaQueryWrapper<BizBenchmarkPrice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizBenchmarkPrice::getMaterialName, dto.getMaterialName());
        if (StringUtils.hasText(dto.getSpec())) {
            wrapper.eq(BizBenchmarkPrice::getSpec, dto.getSpec());
        } else {
            wrapper.isNull(BizBenchmarkPrice::getSpec);
        }
        wrapper.eq(BizBenchmarkPrice::getStatus, 1);
        BizBenchmarkPrice existing = benchmarkPriceMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setBenchmarkPrice(dto.getBenchmarkPrice());
            existing.setUpdateType(2);
            existing.setUnit(dto.getUnit());
            existing.setCreatorId(SecurityUtils.getCurrentUserId());
            benchmarkPriceMapper.updateById(existing);
            return existing.getId();
        } else {
            BizBenchmarkPrice bp = new BizBenchmarkPrice();
            bp.setMaterialName(dto.getMaterialName());
            bp.setSpec(dto.getSpec());
            bp.setUnit(dto.getUnit());
            bp.setBenchmarkPrice(dto.getBenchmarkPrice());
            bp.setUpdateType(2);
            bp.setStatus(1);
            bp.setCreatorId(SecurityUtils.getCurrentUserId());
            benchmarkPriceMapper.insert(bp);
            return bp.getId();
        }
    }

    @Override
    @Transactional
    public void submitBenchmarkPriceApproval(Long id) {
        BizBenchmarkPrice bp = benchmarkPriceMapper.selectById(id);
        if (bp == null) throw new BusinessException(404, "基准价记录不存在");
        if (bp.getUpdateType() != 2) throw new BusinessException("只有手动修改的基准价需要审批");
        bp.setStatus(2); // pending approval
        benchmarkPriceMapper.updateById(bp);
    }

    @Override
    @Transactional
    public void approveBenchmarkPrice(Long id, String comment) {
        BizBenchmarkPrice bp = benchmarkPriceMapper.selectById(id);
        if (bp == null) throw new BusinessException(404, "基准价记录不存在");
        if (bp.getStatus() != 2) throw new BusinessException("当前状态不支持审批");
        if (comment == null || comment.length() < 2) throw new BusinessException("审批意见至少2个字符");
        bp.setStatus(1); // approved (active)
        benchmarkPriceMapper.updateById(bp);
        log.info("Benchmark price approved for material: {}", bp.getMaterialName());
    }

    @Override
    @Transactional
    public void autoUpdateBenchmarkFromContract(Long contractId) {
        BizContract contract = contractMapper.selectById(contractId);
        if (contract == null) return;

        LambdaQueryWrapper<BizContractItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(BizContractItem::getContractId, contractId);
        List<BizContractItem> items = contractItemMapper.selectList(itemWrapper);

        for (BizContractItem item : items) {
            if (!StringUtils.hasText(item.getMaterialName()) || item.getUnitPrice() == null) continue;

            LambdaQueryWrapper<BizBenchmarkPrice> bpWrapper = new LambdaQueryWrapper<>();
            bpWrapper.eq(BizBenchmarkPrice::getMaterialName, item.getMaterialName());
            if (StringUtils.hasText(item.getSpec())) {
                bpWrapper.eq(BizBenchmarkPrice::getSpec, item.getSpec());
            }
            bpWrapper.eq(BizBenchmarkPrice::getStatus, 1);
            BizBenchmarkPrice existing = benchmarkPriceMapper.selectOne(bpWrapper);

            if (existing != null) {
                existing.setBenchmarkPrice(item.getUnitPrice());
                existing.setUpdateType(1);
                existing.setSourceContractId(contractId);
                existing.setSourceSupplierId(contract.getSupplierId());
                benchmarkPriceMapper.updateById(existing);
            } else {
                BizBenchmarkPrice bp = new BizBenchmarkPrice();
                bp.setMaterialName(item.getMaterialName());
                bp.setSpec(item.getSpec());
                bp.setUnit(item.getUnit());
                bp.setBenchmarkPrice(item.getUnitPrice());
                bp.setUpdateType(1);
                bp.setSourceContractId(contractId);
                bp.setSourceSupplierId(contract.getSupplierId());
                bp.setStatus(1);
                benchmarkPriceMapper.insert(bp);
            }
        }
        log.info("Benchmark prices auto-updated from contract {}", contract.getContractNo());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BenchmarkPriceVO> getMaterialPriceHistory(String materialName, String spec) {
        LambdaQueryWrapper<BizBenchmarkPrice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizBenchmarkPrice::getMaterialName, materialName);
        if (StringUtils.hasText(spec)) {
            wrapper.eq(BizBenchmarkPrice::getSpec, spec);
        }
        wrapper.orderByDesc(BizBenchmarkPrice::getUpdatedAt);
        return benchmarkPriceMapper.selectList(wrapper).stream().map(this::toBenchmarkVO).collect(Collectors.toList());
    }

    private PurchaseListVO toVO(BizPurchaseList pl) {
        PurchaseListVO vo = new PurchaseListVO();
        vo.setId(pl.getId());
        vo.setProjectId(pl.getProjectId());
        vo.setContractId(pl.getContractId());
        vo.setListNo(pl.getListNo());
        vo.setStatus(pl.getStatus());
        vo.setStatusName(STATUS_MAP.getOrDefault(pl.getStatus(), "未知"));
        vo.setTotalAmount(pl.getTotalAmount());
        vo.setCreatorId(pl.getCreatorId());
        vo.setApprovedAt(pl.getApprovedAt());
        vo.setCreatedAt(pl.getCreatedAt());

        if (pl.getProjectId() != null) {
            BizProject project = projectMapper.selectById(pl.getProjectId());
            if (project != null) vo.setProjectName(project.getProjectName());
        }
        if (pl.getCreatorId() != null) {
            SysUser user = userMapper.selectById(pl.getCreatorId());
            if (user != null) vo.setCreatorName(user.getRealName());
        }
        vo.setItems(Collections.emptyList());
        return vo;
    }

    private PurchaseListVO.PurchaseItemVO toItemVO(BizPurchaseItem item) {
        PurchaseListVO.PurchaseItemVO vo = new PurchaseListVO.PurchaseItemVO();
        vo.setId(item.getId());
        vo.setMaterialName(item.getMaterialName());
        vo.setSpec(item.getSpec());
        vo.setUnit(item.getUnit());
        vo.setQuantity(item.getQuantity());
        vo.setEstimatedPrice(item.getEstimatedPrice());
        vo.setRemark(item.getRemark());
        return vo;
    }

    private BenchmarkPriceVO toBenchmarkVO(BizBenchmarkPrice bp) {
        BenchmarkPriceVO vo = new BenchmarkPriceVO();
        vo.setId(bp.getId());
        vo.setMaterialName(bp.getMaterialName());
        vo.setSpec(bp.getSpec());
        vo.setUnit(bp.getUnit());
        vo.setBenchmarkPrice(bp.getBenchmarkPrice());
        vo.setUpdateType(bp.getUpdateType());
        vo.setUpdateTypeName(bp.getUpdateType() == 1 ? "自动(合同)" : "手动修改");
        vo.setSourceContractId(bp.getSourceContractId());
        vo.setSourceSupplierId(bp.getSourceSupplierId());
        vo.setStatus(bp.getStatus());
        vo.setCreatedAt(bp.getCreatedAt());
        vo.setUpdatedAt(bp.getUpdatedAt());
        return vo;
    }
}
