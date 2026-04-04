package com.mochu.business.purchase.controller;

import com.mochu.business.purchase.dto.BenchmarkPriceDTO;
import com.mochu.business.purchase.dto.PurchaseListCreateDTO;
import com.mochu.business.purchase.dto.PurchaseListQueryDTO;
import com.mochu.business.purchase.service.BizPurchaseService;
import com.mochu.business.purchase.vo.BenchmarkPriceVO;
import com.mochu.business.purchase.vo.PurchaseListVO;
import com.mochu.common.result.PageResult;
import com.mochu.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
public class BizPurchaseController {

    private final BizPurchaseService purchaseService;

    @GetMapping
    @PreAuthorize("hasAuthority('purchase:view')")
    public R<PageResult<PurchaseListVO>> list(PurchaseListQueryDTO query) {
        return R.ok(purchaseService.listPurchaseLists(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('purchase:view')")
    public R<PurchaseListVO> getById(@PathVariable Long id) {
        return R.ok(purchaseService.getPurchaseListById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('purchase:create')")
    public R<Long> create(@Valid @RequestBody PurchaseListCreateDTO dto) {
        return R.ok(purchaseService.createPurchaseList(dto));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('purchase:create')")
    public R<Void> submit(@PathVariable Long id) {
        purchaseService.submitForApproval(id);
        return R.ok();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('purchase:approve')")
    public R<Void> approve(@PathVariable Long id, @RequestParam String comment) {
        purchaseService.approve(id, comment);
        return R.ok();
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('purchase:approve')")
    public R<Void> reject(@PathVariable Long id, @RequestParam String comment) {
        purchaseService.reject(id, comment);
        return R.ok();
    }

    @PostMapping("/{id}/change")
    @PreAuthorize("hasAuthority('purchase:create')")
    public R<Void> requestChange(@PathVariable Long id) {
        purchaseService.requestChange(id);
        return R.ok();
    }

    // Benchmark price endpoints
    @GetMapping("/benchmark-prices")
    @PreAuthorize("hasAuthority('purchase:view')")
    public R<List<BenchmarkPriceVO>> listBenchmarkPrices(@RequestParam(required = false) String keyword) {
        return R.ok(purchaseService.listBenchmarkPrices(keyword));
    }

    @PostMapping("/benchmark-prices")
    @PreAuthorize("hasAuthority('purchase:create')")
    public R<Long> createBenchmarkPrice(@Valid @RequestBody BenchmarkPriceDTO dto) {
        return R.ok(purchaseService.createOrUpdateBenchmarkPrice(dto));
    }

    @PostMapping("/benchmark-prices/{id}/submit")
    @PreAuthorize("hasAuthority('purchase:create')")
    public R<Void> submitBenchmarkPrice(@PathVariable Long id) {
        purchaseService.submitBenchmarkPriceApproval(id);
        return R.ok();
    }

    @PostMapping("/benchmark-prices/{id}/approve")
    @PreAuthorize("hasAuthority('purchase:approve')")
    public R<Void> approveBenchmarkPrice(@PathVariable Long id, @RequestParam String comment) {
        purchaseService.approveBenchmarkPrice(id, comment);
        return R.ok();
    }

    @GetMapping("/price-history")
    @PreAuthorize("hasAuthority('purchase:view')")
    public R<List<BenchmarkPriceVO>> priceHistory(@RequestParam String materialName,
                                                   @RequestParam(required = false) String spec) {
        return R.ok(purchaseService.getMaterialPriceHistory(materialName, spec));
    }
}
