package com.mochu.common.constant;

public final class Constants {
    private Constants() {}

    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final int LOCK_MINUTES = 30;
    public static final int SMS_CODE_LENGTH = 6;
    public static final int SMS_CODE_EXPIRE_SECONDS = 300;
    public static final int SMS_RESEND_INTERVAL_SECONDS = 60;
    public static final long TOKEN_EXPIRE_MILLIS = 30L * 24 * 60 * 60 * 1000;
    public static final long TOKEN_REFRESH_THRESHOLD_MILLIS = 7L * 24 * 60 * 60 * 1000;

    public static final String REDIS_TOKEN_PREFIX = "auth:token:";
    public static final String REDIS_PERMISSIONS_PREFIX = "user:permissions:";
    public static final String REDIS_LOGIN_FAIL_PREFIX = "auth:login_fail:";
    public static final String REDIS_SMS_PREFIX = "sms:phone:";
    public static final String REDIS_BIZ_NO_PREFIX = "biz_no:";
    public static final String REDIS_CONTACT_PREFIX = "contact:list:";
    public static final String REDIS_TODO_COUNT_PREFIX = "home:todo_count:";

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public static final int DATA_SCOPE_ALL = 1;
    public static final int DATA_SCOPE_DEPT = 2;
    public static final int DATA_SCOPE_SELF = 3;
    public static final int DATA_SCOPE_CUSTOM = 4;
}
