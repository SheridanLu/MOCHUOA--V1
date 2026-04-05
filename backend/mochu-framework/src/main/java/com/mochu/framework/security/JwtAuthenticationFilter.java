package com.mochu.framework.security;

import com.mochu.common.constant.Constants;
import com.mochu.common.utils.LoginUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String token = resolveToken(request);
            if (token != null && jwtUtils.validateToken(token)) {
                Long userId = jwtUtils.getUserIdFromToken(token);
                String clientType = jwtUtils.getClientTypeFromToken(token);
                String redisKey = Constants.REDIS_TOKEN_PREFIX + userId + ":" + clientType;
                String storedToken = redisTemplate.opsForValue().get(redisKey);

                if (token.equals(storedToken)) {
                    String permKey = Constants.REDIS_PERMISSIONS_PREFIX + userId;
                    Set<String> permissions = redisTemplate.opsForSet().members(permKey);

                    LoginUser loginUser = new LoginUser();
                    loginUser.setUserId(userId);
                    loginUser.setUsername(jwtUtils.getUsernameFromToken(token));
                    loginUser.setClientType(clientType);
                    loginUser.setPermissions(permissions != null ? permissions : Set.of());
                    loginUser.setStatus(1);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    if (jwtUtils.shouldRefreshToken(token)) {
                        String newToken = jwtUtils.generateToken(userId, loginUser.getUsername(), clientType);
                        redisTemplate.opsForValue().set(redisKey, newToken, 30, TimeUnit.DAYS);
                        response.setHeader("X-New-Token", newToken);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("JWT 认证过滤器异常，跳过认证继续处理请求: {}", e.getMessage());
            // 不要抛出异常，让请求继续。对于 permitAll 的端点（如登录），无需认证即可通过。
            // 对于需要认证的端点，SecurityContext 中没有认证信息，Spring Security 会返回 401。
        }
        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
