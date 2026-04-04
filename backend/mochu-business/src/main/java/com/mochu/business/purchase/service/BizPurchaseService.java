package com.mochu.business.purchase.service;

import com.mochu.business.purchase.dto.BenchmarkPriceDTO;
import com.mochu.business.purchase.dto.PurchaseListCreateDTO;
import com.mochu.business.purchase.dto.PurchaseListQueryDTO;
import com.mochu.business.purchase.vo.BenchmarkPriceVO;
import com.mochu.business.purchase.vo.PurchaseListVO;
import com.mochu.common.result.PageResult;

import java.util.List;

public interface BizPurchaseService {
    PageResult<PurchaseListVO> listPurchaseLists(PurchaseListQueryDTO query);
    PurchaseListVO getPurchaseListById(Long id);
    Long createPurchaseList(PurchaseListCreateDTO dto);
    void submitForApproval(Long id);
    void approve(Long id, String comment);
    void reject(Long id, String comment);
    void requestChange(Long id);
    List<BenchmarkPriceVO> listBenchmarkPrices(String keyword);
    Long createOrUpdateBenchmarkPrice(BenchmarkPriceDTO dto);
    void submitBenchmarkPriceApproval(Long id);
    void approveBenchmarkPrice(Long id, String comment);
    void autoUpdateBenchmarkFromContract(Long contractId);
    List<BenchmarkPriceVO> getMaterialPriceHistory(String materialName, String spec);
}
