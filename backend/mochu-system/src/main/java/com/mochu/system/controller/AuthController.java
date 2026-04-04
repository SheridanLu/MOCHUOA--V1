package com.mochu.system.controller;

import com.mochu.common.result.R;
import com.mochu.system.dto.CheckAccountDTO;
import com.mochu.system.dto.LoginDTO;
import com.mochu.system.service.AuthService;
import com.mochu.system.vo.CheckAccountVO;
import com.mochu.system.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/check-account")
    public R<CheckAccountVO> checkAccount(@Valid @RequestBody CheckAccountDTO dto) {
        return R.ok(authService.checkAccount(dto));
    }

    @PostMapping("/login/password")
    public R<LoginVO> loginByPassword(@Valid @RequestBody LoginDTO dto) {
        return R.ok(authService.loginByPassword(dto));
    }

    @PostMapping("/login/sms")
    public R<LoginVO> loginBySms(@Valid @RequestBody LoginDTO dto) {
        return R.ok(authService.loginBySms(dto));
    }

    @PostMapping("/sms/send")
    public R<Void> sendSmsCode(@RequestParam String phone) {
        authService.sendSmsCode(phone);
        return R.ok();
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }

    @PostMapping("/reset-password")
    public R<Void> resetPassword(@RequestParam String account, @RequestParam String smsCode, @RequestParam String newPassword) {
        authService.resetPassword(account, smsCode, newPassword);
        return R.ok();
    }
}
