package com.mochu.business.supplier.service;

import com.mochu.business.supplier.dto.SupplierCreateDTO;
import com.mochu.business.supplier.dto.SupplierQueryDTO;
import com.mochu.business.supplier.dto.SupplierUpdateDTO;
import com.mochu.business.supplier.vo.SupplierVO;
import com.mochu.common.result.PageResult;

import java.util.List;

public interface BizSupplierService {
    PageResult<SupplierVO> listSuppliers(SupplierQueryDTO query);
    SupplierVO getSupplierById(Long id);
    Long createSupplier(SupplierCreateDTO dto);
    void updateSupplier(Long id, SupplierUpdateDTO dto);
    void updateStatus(Long id, Integer status);
    List<SupplierVO> listAllEnabled();
}
