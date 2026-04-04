package com.mochu.business.supplier.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.business.supplier.dto.SupplierCreateDTO;
import com.mochu.business.supplier.dto.SupplierQueryDTO;
import com.mochu.business.supplier.dto.SupplierUpdateDTO;
import com.mochu.business.supplier.entity.BizSupplier;
import com.mochu.business.supplier.mapper.BizSupplierMapper;
import com.mochu.business.supplier.service.BizSupplierService;
import com.mochu.business.supplier.vo.SupplierVO;
import com.mochu.common.constant.Constants;
import com.mochu.common.exception.BusinessException;
import com.mochu.common.result.PageResult;
import com.mochu.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BizSupplierServiceImpl implements BizSupplierService {

    private final BizSupplierMapper supplierMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResult<SupplierVO> listSuppliers(SupplierQueryDTO query) {
        int page = query.getPage() != null ? query.getPage() : Constants.DEFAULT_PAGE;
        int size = Math.min(query.getSize() != null ? query.getSize() : Constants.DEFAULT_SIZE, Constants.MAX_SIZE);

        LambdaQueryWrapper<BizSupplier> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(BizSupplier::getSupplierName, query.getKeyword())
                    .or().like(BizSupplier::getSupplierCode, query.getKeyword())
                    .or().like(BizSupplier::getContactPerson, query.getKeyword()));
        }
        if (query.getCategory() != null) wrapper.eq(BizSupplier::getCategory, query.getCategory());
        if (query.getStatus() != null) wrapper.eq(BizSupplier::getStatus, query.getStatus());
        wrapper.orderByDesc(BizSupplier::getCreatedAt);

        IPage<BizSupplier> pageResult = supplierMapper.selectPage(new Page<>(page, size), wrapper);
        List<SupplierVO> voList = pageResult.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(voList, pageResult.getTotal(), page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierVO getSupplierById(Long id) {
        BizSupplier supplier = supplierMapper.selectById(id);
        if (supplier == null) throw new BusinessException(404, "供应商不存在");
        return toVO(supplier);
    }

    @Override
    @Transactional
    public Long createSupplier(SupplierCreateDTO dto) {
        LambdaQueryWrapper<BizSupplier> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(BizSupplier::getSupplierCode, dto.getSupplierCode());
        if (supplierMapper.selectCount(checkWrapper) > 0) {
            throw new BusinessException("供应商编码已存在");
        }

        BizSupplier supplier = new BizSupplier();
        supplier.setSupplierName(dto.getSupplierName());
        supplier.setSupplierCode(dto.getSupplierCode());
        supplier.setContactPerson(dto.getContactPerson());
        supplier.setContactPhone(dto.getContactPhone());
        supplier.setAddress(dto.getAddress());
        supplier.setBankName(dto.getBankName());
        supplier.setBankAccount(dto.getBankAccount());
        supplier.setTaxNo(dto.getTaxNo());
        supplier.setCategory(dto.getCategory());
        supplier.setRating(dto.getRating());
        supplier.setStatus(1);
        supplier.setCreatorId(SecurityUtils.getCurrentUserId());
        supplierMapper.insert(supplier);
        return supplier.getId();
    }

    @Override
    @Transactional
    public void updateSupplier(Long id, SupplierUpdateDTO dto) {
        BizSupplier supplier = supplierMapper.selectById(id);
        if (supplier == null) throw new BusinessException(404, "供应商不存在");
        supplier.setSupplierName(dto.getSupplierName());
        supplier.setContactPerson(dto.getContactPerson());
        supplier.setContactPhone(dto.getContactPhone());
        supplier.setAddress(dto.getAddress());
        supplier.setBankName(dto.getBankName());
        supplier.setBankAccount(dto.getBankAccount());
        supplier.setTaxNo(dto.getTaxNo());
        supplier.setCategory(dto.getCategory());
        supplier.setRating(dto.getRating());
        supplierMapper.updateById(supplier);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        BizSupplier supplier = supplierMapper.selectById(id);
        if (supplier == null) throw new BusinessException(404, "供应商不存在");
        supplier.setStatus(status);
        supplierMapper.updateById(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierVO> listAllEnabled() {
        LambdaQueryWrapper<BizSupplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizSupplier::getStatus, 1).orderByAsc(BizSupplier::getSupplierName);
        return supplierMapper.selectList(wrapper).stream().map(this::toVO).collect(Collectors.toList());
    }

    private SupplierVO toVO(BizSupplier entity) {
        SupplierVO vo = new SupplierVO();
        vo.setId(entity.getId());
        vo.setSupplierName(entity.getSupplierName());
        vo.setSupplierCode(entity.getSupplierCode());
        vo.setContactPerson(entity.getContactPerson());
        vo.setContactPhone(entity.getContactPhone());
        vo.setAddress(entity.getAddress());
        vo.setBankName(entity.getBankName());
        vo.setBankAccount(entity.getBankAccount());
        vo.setTaxNo(entity.getTaxNo());
        vo.setCategory(entity.getCategory());
        vo.setRating(entity.getRating());
        vo.setStatus(entity.getStatus());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}
