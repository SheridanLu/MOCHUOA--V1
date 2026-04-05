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
        // Redis 读取失败计数（容错：Redis 不可用时跳过锁定检查）
        int failCount = 0;
        try {
            String failKey = Constants.REDIS_LOGIN_FAIL_PREFIX + dto.getAccount();
            String failCountStr = redisTemplate.opsForValue().get(failKey);
            failCount = failCountStr != null ? Integer.parseInt(failCountStr) : 0;
            if (failCount >= Constants.MAX_LOGIN_ATTEMPTS) {
                Long ttl = redisTemplate.getExpire(failKey, TimeUnit.MINUTES);
                throw new BusinessException(423, "账号已锁定，请" + (ttl != null ? ttl : Constants.LOCK_MINUTES) + "分钟后重试");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Redis 读取登录失败计数异常，跳过锁定检查: {}", e.getMessage());
        }

        SysUser user = userMapper.selectByAccount(dto.getAccount());
        if (user == null) {
            throw new BusinessException("账号不存在");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 密码校验（容错：hash 为空或格式错误时给出明确提示）
        String hash = user.getPasswordHash();
        if (hash == null || hash.isEmpty()) {
            log.error("用户 {} 密码 hash 为空", dto.getAccount());
            throw new BusinessException("账号数据异常，请联系管理员");
        }
        try {
            if (!passwordEncoder.matches(dto.getPassword(), hash)) {
                incrementFailCount(dto.getAccount(), failCount);
                int remaining = Constants.MAX_LOGIN_ATTEMPTS - failCount - 1;
                if (remaining <= 0) {
                    throw new BusinessException(423, "密码错误次数过多，账号已锁定" + Constants.LOCK_MINUTES + "分钟");
                }
                throw new BusinessException("密码错误，还剩" + remaining + "次机会");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("密码校验异常 account={}, hashPrefix={}: {}", dto.getAccount(),
                    hash.length() > 10 ? hash.substring(0, 10) + "..." : hash, e.getMessage());
            throw new BusinessException("密码校验失败，请联系管理员");
        }

        // 清除失败计数
        try {
            redisTemplate.delete(Constants.REDIS_LOGIN_FAIL_PREFIX + dto.getAccount());
        } catch (Exception e) {
            log.warn("Redis 清除失败计数异常: {}", e.getMessage());
        }

        return buildLoginVO(user, dto.getClientType());
    }

    private void incrementFailCount(String account, int currentCount) {
        try {
            String failKey = Constants.REDIS_LOGIN_FAIL_PREFIX + account;
            redisTemplate.opsForValue().set(failKey, String.valueOf(currentCount + 1), Constants.LOCK_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis 递增失败计数异常: {}", e.getMessage());
        }
    }

    @Override
    public LoginVO loginBySms(LoginDTO dto) {
        String smsKey = Constants.REDIS_SMS_PREFIX + dto.getAccount();
        String storedCode;
        try {
            storedCode = redisTemplate.opsForValue().get(smsKey);
        } catch (Exception e) {
            log.error("Redis 读取验证码异常: {}", e.getMessage());
            throw new BusinessException("验证码服务暂不可用，请稍后重试");
        }
        if (storedCode == null || !storedCode.equals(dto.getSmsCode())) {
            throw new BusinessException("验证码错误或已过期");
        }
        try {
            redisTemplate.delete(smsKey);
        } catch (Exception e) {
            log.warn("Redis 删除验证码异常: {}", e.getMessage());
        }

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
        try {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(intervalKey))) {
                throw new BusinessException("请60秒后重试");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Redis 检查发送间隔异常: {}", e.getMessage());
        }
        SysUser user = userMapper.selectByAccount(phone);
        if (user == null) {
            throw new BusinessException("该手机号未注册");
        }
        String code = String.format("%06d", new Random().nextInt(1000000));
        String smsKey = Constants.REDIS_SMS_PREFIX + phone;
        try {
            redisTemplate.opsForValue().set(smsKey, code, Constants.SMS_CODE_EXPIRE_SECONDS, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(intervalKey, "1", Constants.SMS_RESEND_INTERVAL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Redis 存储验证码异常: {}", e.getMessage());
            throw new BusinessException("验证码发送失败，请稍后重试");
        }
        log.info("SMS code sent to {}: {} (dev mode)", phone, code);
    }

    @Override
    public void logout() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null) {
            try {
                String redisKey = Constants.REDIS_TOKEN_PREFIX + loginUser.getUserId() + ":" + loginUser.getClientType();
                redisTemplate.delete(redisKey);
                String permKey = Constants.REDIS_PERMISSIONS_PREFIX + loginUser.getUserId();
                redisTemplate.delete(permKey);
            } catch (Exception e) {
                log.warn("Redis 清除登录信息异常: {}", e.getMessage());
            }
        }
    }

    @Override
    public void resetPassword(String account, String smsCode, String newPassword) {
        String smsKey = Constants.REDIS_SMS_PREFIX + account;
        String storedCode;
        try {
            storedCode = redisTemplate.opsForValue().get(smsKey);
        } catch (Exception e) {
            throw new BusinessException("验证码服务暂不可用，请稍后重试");
        }
        if (storedCode == null || !storedCode.equals(smsCode)) {
            throw new BusinessException("验证码错误或已过期");
        }
        try {
            redisTemplate.delete(smsKey);
        } catch (Exception e) {
            log.warn("Redis 删除验证码异常: {}", e.getMessage());
        }

        SysUser user = userMapper.selectByAccount(account);
        if (user == null) {
            throw new BusinessException("账号不存在");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        try {
            Set<String> keys = redisTemplate.keys(Constants.REDIS_TOKEN_PREFIX + user.getId() + ":*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            redisTemplate.delete(Constants.REDIS_PERMISSIONS_PREFIX + user.getId());
        } catch (Exception e) {
            log.warn("Redis 清除 token 异常: {}", e.getMessage());
        }
    }

    private LoginVO buildLoginVO(SysUser user, String clientType) {
        // 生成 JWT Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), clientType);

        // 存储 Token 到 Redis（容错：Redis 不可用时仍返回 token，只是无法做服务端校验）
        try {
            String tokenKey = Constants.REDIS_TOKEN_PREFIX + user.getId() + ":" + clientType;
            redisTemplate.opsForValue().set(tokenKey, token, 30, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("Redis 存储 token 异常，登录仍可继续: {}", e.getMessage());
        }

        // 查询权限（容错：查询失败返回空权限集合）
        Set<String> permCodes;
        try {
            permCodes = permissionMapper.selectPermCodesByUserId(user.getId());
            if (permCodes == null) {
                permCodes = new HashSet<>();
            }
        } catch (Exception e) {
            log.error("查询用户权限异常 userId={}: {}", user.getId(), e.getMessage());
            permCodes = new HashSet<>();
        }

        // 缓存权限到 Redis
        try {
            String permKey = Constants.REDIS_PERMISSIONS_PREFIX + user.getId();
            redisTemplate.delete(permKey);
            if (!permCodes.isEmpty()) {
                redisTemplate.opsForSet().add(permKey, permCodes.toArray(new String[0]));
                redisTemplate.expire(permKey, 30, TimeUnit.DAYS);
            }
        } catch (Exception e) {
            log.warn("Redis 缓存权限异常: {}", e.getMessage());
        }

        // 更新最后登录时间
        try {
            user.setLastLoginAt(LocalDateTime.now());
            userMapper.updateById(user);
        } catch (Exception e) {
            log.warn("更新最后登录时间异常: {}", e.getMessage());
        }

        // 构建 UserVO
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

        // 查询部门名称
        if (user.getDeptId() != null) {
            try {
                SysDepartment dept = departmentMapper.selectById(user.getDeptId());
                if (dept != null) userVO.setDeptName(dept.getDeptName());
            } catch (Exception e) {
                log.warn("查询部门名称异常: {}", e.getMessage());
            }
        }

        // 查询角色（容错：查询失败返回空列表）
        List<RoleVO> roleVOList;
        try {
            List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());
            roleVOList = (roles != null ? roles : Collections.<SysRole>emptyList()).stream().map(r -> {
                RoleVO rv = new RoleVO();
                rv.setId(r.getId());
                rv.setRoleCode(r.getRoleCode());
                rv.setRoleName(r.getRoleName());
                rv.setDescription(r.getDescription());
                rv.setDataScope(r.getDataScope());
                rv.setStatus(r.getStatus());
                return rv;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询用户角色异常 userId={}: {}", user.getId(), e.getMessage());
            roleVOList = new ArrayList<>();
        }
        userVO.setRoles(roleVOList);

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

        Set<String> permCodes;
        try {
            permCodes = permissionMapper.selectPermCodesByUserId(user.getId());
            if (permCodes == null) permCodes = new HashSet<>();
        } catch (Exception e) {
            log.error("查询权限异常: {}", e.getMessage());
            permCodes = new HashSet<>();
        }

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
            try {
                SysDepartment dept = departmentMapper.selectById(user.getDeptId());
                if (dept != null) userVO.setDeptName(dept.getDeptName());
            } catch (Exception e) {
                log.warn("查询部门名称异常: {}", e.getMessage());
            }
        }

        List<RoleVO> roleVOList;
        try {
            List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());
            roleVOList = (roles != null ? roles : Collections.<SysRole>emptyList()).stream().map(r -> {
                RoleVO rv = new RoleVO();
                rv.setId(r.getId());
                rv.setRoleCode(r.getRoleCode());
                rv.setRoleName(r.getRoleName());
                rv.setDescription(r.getDescription());
                rv.setDataScope(r.getDataScope());
                rv.setStatus(r.getStatus());
                return rv;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询角色异常: {}", e.getMessage());
            roleVOList = new ArrayList<>();
        }
        userVO.setRoles(roleVOList);

        LoginVO loginVO = new LoginVO();
        loginVO.setUserInfo(userVO);
        loginVO.setPermissions(permCodes);
        return loginVO;
    }
}
