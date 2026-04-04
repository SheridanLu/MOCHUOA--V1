package com.mochu.business.contract.service;

import com.mochu.business.contract.dto.ContractCreateDTO;
import com.mochu.business.contract.dto.ContractQueryDTO;
import com.mochu.business.contract.dto.SupplementCreateDTO;
import com.mochu.business.contract.vo.ContractVO;
import com.mochu.common.result.PageResult;

public interface BizContractService {
    PageResult<ContractVO> listContracts(ContractQueryDTO query);
    ContractVO getContractById(Long id);
    Long createContract(ContractCreateDTO dto);
    void submitForApproval(Long id);
    void approve(Long id, String comment);
    void reject(Long id, String comment);
    void terminate(Long id, String reason);
    Long createSupplement(Long contractId, SupplementCreateDTO dto);
}
