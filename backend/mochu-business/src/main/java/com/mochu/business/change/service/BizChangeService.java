package com.mochu.business.change.service;
import com.mochu.business.change.dto.*;
import com.mochu.business.change.vo.*;
import com.mochu.common.result.PageResult;
import java.util.List;

public interface BizChangeService {
    Long createSiteVisa(SiteVisaCreateDTO dto);
    void approveSiteVisa(Long id, String comment);
    void rejectSiteVisa(Long id, String comment);
    Long createOwnerChange(OwnerChangeCreateDTO dto);
    void approveOwnerChange(Long id, String comment);
    void rejectOwnerChange(Long id, String comment);
    Long createLaborVisa(LaborVisaCreateDTO dto);
    void approveLaborVisa(Long id, String comment);
    void rejectLaborVisa(Long id, String comment);
    PageResult<ChangeLedgerVO> queryChangeLedger(ChangeQueryDTO query);
}
