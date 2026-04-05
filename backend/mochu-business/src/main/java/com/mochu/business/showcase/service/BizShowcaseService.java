package com.mochu.business.showcase.service;

import com.mochu.business.showcase.dto.ShowcaseCreateDTO;
import com.mochu.business.showcase.dto.ShowcaseQueryDTO;
import com.mochu.business.showcase.vo.ShowcaseVO;
import com.mochu.common.result.PageResult;

public interface BizShowcaseService {
    PageResult<ShowcaseVO> listShowcases(ShowcaseQueryDTO query);
    ShowcaseVO getShowcaseDetail(Long id);
    Long createShowcase(ShowcaseCreateDTO dto);
    void submitForApproval(Long id);
    void approveShowcase(Long id, String comment);
    void rejectShowcase(Long id, String comment);
    void setVisibility(Long id, Integer visibility);
}
