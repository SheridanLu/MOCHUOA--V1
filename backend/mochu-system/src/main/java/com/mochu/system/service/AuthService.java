package com.mochu.system.service;

import com.mochu.system.dto.CheckAccountDTO;
import com.mochu.system.dto.LoginDTO;
import com.mochu.system.vo.CheckAccountVO;
import com.mochu.system.vo.LoginVO;

public interface AuthService {
    CheckAccountVO checkAccount(CheckAccountDTO dto);
    LoginVO loginByPassword(LoginDTO dto);
    LoginVO loginBySms(LoginDTO dto);
    void sendSmsCode(String phone);
    void logout();
    void resetPassword(String account, String smsCode, String newPassword);
}
