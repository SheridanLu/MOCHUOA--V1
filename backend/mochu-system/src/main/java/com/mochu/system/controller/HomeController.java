package com.mochu.system.controller;

import com.mochu.common.result.R;
import com.mochu.system.service.HomeService;
import com.mochu.system.vo.HomeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    public R<HomeVO> home() { return R.ok(homeService.getHomeData()); }
}
