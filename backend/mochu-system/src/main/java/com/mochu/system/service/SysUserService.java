package com.mochu.system.service;

import com.mochu.common.result.PageResult;
import com.mochu.system.dto.UserCreateDTO;
import com.mochu.system.dto.UserQueryDTO;
import com.mochu.system.dto.UserUpdateDTO;
import com.mochu.system.vo.UserVO;

public interface SysUserService {
    PageResult<UserVO> listUsers(UserQueryDTO query);
    UserVO getUserById(Long id);
    Long createUser(UserCreateDTO dto);
    void updateUser(Long id, UserUpdateDTO dto);
    void updateStatus(Long id, Integer status);
    void resetPassword(Long id);
}
