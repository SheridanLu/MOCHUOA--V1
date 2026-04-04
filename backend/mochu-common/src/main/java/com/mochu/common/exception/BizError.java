package com.mochu.common.exception;

import lombok.Getter;

/**
 * 业务异常错误码枚举
 */
@Getter
public enum BizError {

    // 项目模块 10xxx
    PROJECT_STATUS_NOT_ALLOWED(10001, "项目状态不允许此操作"),
    PROJECT_INVEST_LIMIT_EXCEEDED(10002, "超出虚拟项目投入限额"),

    // 合同模块 20xxx
    CONTRACT_AMOUNT_MISMATCH(20001, "合同金额与税率计算不一致"),
    CONTRACT_OVER_QUANTITY(20002, "物资超量需预算员审批"),
    CONTRACT_PAYMENT_RATIO_EXCEEDED(20003, "付款批次比例超过100%"),

    // 物资模块 30xxx
    INBOUND_QUANTITY_EXCEEDED(30001, "入库数量超过合同约定"),
    OUTBOUND_QUANTITY_EXCEEDED(30002, "出库数量超过库存可用量"),
    INVENTORY_INSUFFICIENT(30003, "库存不足"),

    // 财务模块 40xxx
    PAYMENT_AMOUNT_EXCEEDED(40001, "付款金额超过合同可付余额"),
    NO_AVAILABLE_STATEMENT(40002, "无可关联对账单"),

    // 编号模块 50xxx
    BIZ_NO_GENERATE_FAILED(50001, "编号生成失败"),

    // 审批模块 60xxx
    APPROVAL_ALREADY_FINISHED(60001, "审批流程已结束"),
    NOT_CURRENT_APPROVER(60002, "非当前审批人");

    private final int code;
    private final String message;

    BizError(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
