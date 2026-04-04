package com.mochu.business.hr.service;

import com.mochu.business.hr.dto.*;
import com.mochu.business.hr.vo.*;
import com.mochu.common.result.PageResult;

import java.util.List;

public interface BizHrService {
    // Employee
    PageResult<EmployeeVO> listEmployees(EmployeeQueryDTO query);
    EmployeeVO getEmployeeById(Long id);
    Long onboard(EmployeeCreateDTO dto);
    void offboard(Long id);

    // Payroll
    PageResult<PayrollVO> listPayrolls(String period, Integer status, Integer page, Integer size);
    void generateMonthlyPayroll(String period);
    void adjustPayroll(Long id, PayrollAdjustDTO dto);
    void approvePayroll(Long id, String comment);
    void markPayrollPaid(Long id);

    // Reimbursement
    PageResult<ReimbursementVO> listReimbursements(Long employeeId, Integer status, Integer page, Integer size);
    Long createReimbursement(ReimbursementCreateDTO dto);
    void approveReimbursement(Long id, String comment);
    void rejectReimbursement(Long id, String comment);

    // Contract
    List<HrContractVO> listContracts(Long employeeId);
    Long createContract(HrContractCreateDTO dto);
    void renewContract(Long id, HrContractCreateDTO dto);
    List<HrContractVO> getExpiringContracts(int days);

    // Qualification
    List<QualificationVO> listQualifications(Long employeeId);
    Long createQualification(Long employeeId, String qualName, String qualNo, String issueDate, String expireDate, String fileUrl);
    List<QualificationVO> getExpiringQualifications(int days);
}
