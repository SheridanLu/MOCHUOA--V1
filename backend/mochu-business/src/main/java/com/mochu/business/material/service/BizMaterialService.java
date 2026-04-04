package com.mochu.business.material.service;

import com.mochu.business.material.dto.InboundCreateDTO;
import com.mochu.business.material.dto.MaterialQueryDTO;
import com.mochu.business.material.dto.OutboundCreateDTO;
import com.mochu.business.material.dto.ReturnCreateDTO;
import com.mochu.business.material.vo.InboundVO;
import com.mochu.business.material.vo.InventoryVO;
import com.mochu.business.material.vo.OutboundVO;
import com.mochu.business.material.vo.ReturnVO;
import com.mochu.common.result.PageResult;

import java.util.List;

public interface BizMaterialService {
    // Inbound
    PageResult<InboundVO> listInbounds(MaterialQueryDTO query);
    InboundVO getInboundById(Long id);
    Long createInbound(InboundCreateDTO dto);
    void submitInbound(Long id);
    void approveInbound(Long id, String comment);
    void rejectInbound(Long id, String comment);
    void autoCreateInboundDraft(Long contractId);

    // Outbound
    PageResult<OutboundVO> listOutbounds(MaterialQueryDTO query);
    OutboundVO getOutboundById(Long id);
    Long createOutbound(OutboundCreateDTO dto);
    void submitOutbound(Long id);
    void approveOutbound(Long id, String comment);
    void rejectOutbound(Long id, String comment);

    // Return
    PageResult<ReturnVO> listReturns(MaterialQueryDTO query);
    Long createReturn(ReturnCreateDTO dto);
    void approveReturn(Long id, String comment);

    // Inventory
    List<InventoryVO> queryInventory(Long projectId, String keyword, String warehouse);
}
