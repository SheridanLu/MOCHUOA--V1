package com.mochu.system.service.impl;

import com.mochu.common.constant.Constants;
import com.mochu.common.exception.BusinessException;
import com.mochu.common.utils.LoginUser;
import com.mochu.common.utils.SecurityUtils;
import com.mochu.framework.security.JwtUtils;
import com.mochu.system.dto.CheckAccountDTO;
import com.mochu.system.dto.LoginDTO;
import com.mochu.system.entity.SysDepartment;
import com.mochu.system.entity.SysRole;
import com.mochu.system.entity.SysUser;
import com.mochu.system.mapper.SysDepartmentMapper;
import com.mochu.system.mapper.SysPermissionMapper;
import com.mochu.system.mapper.SysRoleMapper;
import com.mochu.system.mapper.SysUserMapper;
import com.mochu.system.service.AuthService;
import com.mochu.system.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysDepartmentMapper departmentMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;

    @Override
    public CheckAccountVO checkAccount(CheckAccountDTO dto) {
        SysUser user = userMapper.selectByAccount(dto.getAccount());
        CheckAccountVO vo = new CheckAccountVO();
        if (user == null) {
            vo.setExists(false);
            return vo;
        }
        vo.setExists(true);
        boolean isPhone = dto.getAccount().matches("^1\\d{10}$");
        vo.setLoginType(isPhone ? "sms" : "password");
        if (isPhone) {
            vo.setMaskedPhone(dto.getAccount().substring(0, 3) + "****" + dto.getAccount().substring(7));
        }
        return vo;
    }

    @Override
    public LoginVO loginByPassword(LoginDTO dto) {
        String failKey = Constants.REDIS_LOGIN_FAIL_PREFIX + dto.getAccount();
        String failCountStr = redisTemplate.opsForValue().get(failKey);
        int failCount = failCountStr != null ? Integer.parseInt(failCountStr) : 0;
        if (failCount >= Constants.MAX_LOGIN_ATTEMPTS) {
            Long ttl = redisTemplate.getExpire(failKey, TimeUnit.MINUTES);
            throw new BusinessException(423, "账号已锁定，请" + (ttl != null ? ttl : Constants.LOCK_MINUTES) + "分钟后重试");
        }

        SysUser user = userMapper.selectByAccount(dto.getAccount());
        if (user == null) {
            throw new BusinessException("账号不存在");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            failCount++;
            redisTemplate.opsForValue().set(failKey, String.valueOf(failCount), Constants.LOCK_MINUTES, TimeUnit.MINUTES);
            int remaining = Constants.MAX_LOGIN_ATTEMPTS - failCount;
            if (remaining <= 0) {
                throw new BusinessException(423, "密码错误次数过多，账号已锁定" + Constants.LOCK_MINUTES + "分钟");
            }
            throw new BusinessException("密码错误，还剩" + remaining + "次机会");
        }

        redisTemplate.delete(failKey);
        return buildLoginVO(user, dto.getClientType());
    }

    @Override
    public LoginVO loginBySms(LoginDTO dto) {
        String smsKey = Constants.REDIS_SMS_PREFIX + dto.getAccount();
        String storedCode = redisTemplate.opsForValue().get(smsKey);
        if (storedCode == null || !storedCode.equals(dto.getSmsCode())) {
            throw new BusinessException("验证码错误或已过期");
        }
        redisTemplate.delete(smsKey);

        SysUser user = userMapper.selectByAccount(dto.getAccount());
        if (user == null) {
            throw new BusinessException("账号不存在");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        return buildLoginVO(user, dto.getClientType());
    }

    @Override
    public void sendSmsCode(String phone) {
        String intervalKey = "sms:interval:" + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(intervalKey))) {
            throw new BusinessException("请60秒后重试");
        }
        SysUser user = userMapper.selectByAccount(phone);
        if (user == null) {
            throw new BusinessException("该手机号未注册");
        }
        String code = String.format("%06d", new Random().nextInt(1000000));
        String smsKey = Constants.REDIS_SMS_PREFIX + phone;
        redisTemplate.opsForValue().set(smsKey, code, Constants.SMS_CODE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(intervalKey, "1", Constants.SMS_RESEND_INTERVAL_SECONDS, TimeUnit.SECONDS);
        log.info("SMS code sent to {}: {} (dev mode)", phone, code);
    }

    @Override
    public void logout() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null) {
            String redisKey = Constants.REDIS_TOKEN_PREFIX + loginUser.getUserId() + ":" + loginUser.getClientType();
            redisTemplate.delete(redisKey);
            String permKey = Constants.REDIS_PERMISSIONS_PREFIX + loginUser.getUserId();
            redisTemplate.delete(permKey);
        }
    }

    @Override
    public void resetPassword(String account, String smsCode, String newPassword) {
        String smsKey = Constants.REDIS_SMS_PREFIX + account;
        String storedCode = redisTemplate.opsForValue().get(smsKey);
        if (storedCode == null || !storedCode.equals(smsCode)) {
            throw new BusinessException("验证码错误或已过期");
        }
        redisTemplate.delete(smsKey);

        SysUser user = userMapper.selectByAccount(account);
        if (user == null) {
            throw new BusinessException("账号不存在");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        Set<String> keys = redisTemplate.keys(Constants.REDIS_TOKEN_PREFIX + user.getId() + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        redisTemplate.delete(Constants.REDIS_PERMISSIONS_PREFIX + user.getId());
    }

    private LoginVO buildLoginVO(SysUser user, String clientType) {
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), clientType);
        String tokenKey = Constants.REDIS_TOKEN_PREFIX + user.getId() + ":" + clientType;
        redisTemplate.opsForValue().set(tokenKey, token, 30, TimeUnit.DAYS);

        Set<String> permCodes = permissionMapper.selectPermCodesByUserId(user.getId());
        String permKey = Constants.REDIS_PERMISSIONS_PREFIX + user.getId();
        redisTemplate.delete(permKey);
        if (!permCodes.isEmpty()) {
            redisTemplate.opsForSet().add(permKey, permCodes.toArray(new String[0]));
            redisTemplate.expire(permKey, 30, TimeUnit.DAYS);
        }

        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setRealName(user.getRealName());
        userVO.setPhone(user.getPhone());
        userVO.setEmail(user.getEmail());
        userVO.setAvatar(user.getAvatar());
        userVO.setDeptId(user.getDeptId());
        userVO.setStatus(user.getStatus());
        userVO.setLastLoginAt(user.getLastLoginAt());
        userVO.setCreatedAt(user.getCreatedAt());

        if (user.getDeptId() != null) {
            SysDepartment dept = departmentMapper.selectById(user.getDeptId());
            if (dept != null) userVO.setDeptName(dept.getDeptName());
        }

        List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());
        userVO.setRoles(roles.stream().map(r -> {
            RoleVO rv = new RoleVO();
            rv.setId(r.getId());
            rv.setRoleCode(r.getRoleCode());
            rv.setRoleName(r.getRoleName());
            rv.setDescription(r.getDescription());
            rv.setDataScope(r.getDataScope());
            rv.setStatus(r.getStatus());
            return rv;
        }).collect(Collectors.toList()));

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(userVO);
        loginVO.setPermissions(permCodes);
        return loginVO;
    }

    @Override
    public LoginVO getCurrentUser() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            throw new BusinessException(401, "未登录");
        }
        SysUser user = userMapper.selectById(loginUser.getUserId());
        if (user == null || user.getStatus() == 0) {
            throw new BusinessException(401, "用户不存在或已被禁用");
        }

        Set<String> permCodes = permissionMapper.selectPermCodesByUserId(user.getId());

        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setRealName(user.getRealName());
        userVO.setPhone(user.getPhone());
        userVO.setEmail(user.getEmail());
        userVO.setAvatar(user.getAvatar());
        userVO.setDeptId(user.getDeptId());
        userVO.setStatus(user.getStatus());
        userVO.setLastLoginAt(user.getLastLoginAt());
        userVO.setCreatedAt(user.getCreatedAt());

        if (user.getDeptId() != null) {
            SysDepartment dept = departmentMapper.selectById(user.getDeptId());
            if (dept != null) userVO.setDeptName(dept.getDeptName());
        }

        List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());
        userVO.setRoles(roles.stream().map(r -> {
            RoleVO rv = new RoleVO();
            rv.setId(r.getId());
            rv.setRoleCode(r.getRoleCode());
            rv.setRoleName(r.getRoleName());
            rv.setDescription(r.getDescription());
            rv.setDataScope(r.getDataScope());
            rv.setStatus(r.getStatus());
            return rv;
        }).collect(Collectors.toList()));

        LoginVO loginVO = new LoginVO();
        loginVO.setUserInfo(userVO);
        loginVO.setPermissions(permCodes);
        return loginVO;
    }
}
