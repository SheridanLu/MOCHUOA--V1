package com.mochu.common.utils;

import com.mochu.common.constant.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class BizNoGenerator {

    private final StringRedisTemplate redisTemplate;

    public String generate(String prefix, String datePattern, int seqLength) {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern(datePattern));
        String redisKey = Constants.REDIS_BIZ_NO_PREFIX + prefix + ":" + datePart;
        Long seq = redisTemplate.opsForValue().increment(redisKey);
        if (seq == null) {
            throw new RuntimeException("编号生成失败: Redis不可用");
        }
        if (seq == 1L) {
            long expireSeconds = datePattern.contains("dd") ? 86400 + 3600 : 32L * 86400;
            redisTemplate.expire(redisKey, java.time.Duration.ofSeconds(expireSeconds));
        }
        String seqStr = String.format("%0" + seqLength + "d", seq);
        return prefix + datePart + seqStr;
    }

    public String generateProjectNo() { return generate("P", "yyMMdd", 3); }
    public String generateVirtualProjectNo() { return generate("V", "yyMM", 3); }
    public String generateIncomeContractNo() { return generate("IC", "yyMMdd", 2); }
    public String generateExpenditureContractNo() { return generate("EC", "yyMMdd", 2); }
    public String generateSupplementNo() { return generate("BC", "yyMMdd", 2); }
    public String generateInboundNo() { return generate("RK", "yyMMdd", 3); }
    public String generateOutboundNo() { return generate("CK", "yyMMdd", 3); }
    public String generateReconciliationNo() { return generate("DZ", "yyMM", 2); }
    public String generateLaborPaymentNo() { return generate("PA", "yyMMdd", 3); }
    public String generateMaterialPaymentNo() { return generate("MP", "yyMMdd", 3); }
}
