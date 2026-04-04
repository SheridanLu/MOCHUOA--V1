package com.mochu.business.hr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.business.contact.entity.BizContact;
import com.mochu.business.contact.service.BizContactService;
import com.mochu.business.hr.dto.*;
import com.mochu.business.hr.entity.*;
import com.mochu.business.hr.mapper.*;
import com.mochu.business.hr.service.BizHrService;
import com.mochu.business.hr.vo.*;
import com.mochu.business.project.entity.BizProject;
import com.mochu.business.project.mapper.BizProjectMapper;
import com.mochu.common.constant.Constants;
import com.mochu.common.exception.BusinessException;
import com.mochu.common.result.PageResult;
import com.mochu.common.utils.BizNoGenerator;
import com.mochu.common.utils.SecurityUtils;
import com.mochu.system.entity.SysDepartment;
import com.mochu.system.entity.SysUser;
import com.mochu.system.mapper.SysDepartmentMapper;
import com.mochu.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizHrServiceImpl implements BizHrService {

    private final BizHrEmployeeMapper employeeMapper;
    private final BizHrPayrollMapper payrollMapper;
    private final BizHrReimbursementMapper reimbursementMapper;
    private final BizHrContractMapper hrContractMapper;
    private final BizHrQualificationMapper qualificationMapper;
    private final SysUserMapper userMapper;
    private final SysDepartmentMapper departmentMapper;
    private final BizProjectMapper projectMapper;
    private final BizContactService contactService;
    private final BizNoGenerator bizNoGenerator;

    private static final Map<Integer, String> EMPLOYEE_STATUS = Map.of(1, "在职", 2, "离职", 3, "试用期");
    private static final Map<Integer, String> PAYROLL_STATUS = Map.of(1, "待调整", 2, "待审批", 3, "财务已审", 4, "已审批", 5, "已发放");
    private static final Map<Integer, String> REIMBURSE_STATUS = Map.of(1, "草稿", 2, "主管审批", 3, "财务审批", 4, "出纳确认", 5, "已完成", 6, "已驳回");
    private static final Map<Integer, String> CONTRACT_TYPE_MAP = Map.of(1, "固定期限", 2, "无固定期限", 3, "试用期", 4, "实习", 5, "劳务派遣");
    private static final Map<Integer, String> CONTRACT_STATUS = Map.of(1, "有效", 2, "已终止", 3, "已续签");

    // ==================== EMPLOYEE ====================

    @Override
    @Transactional(readOnly = true)
    public PageResult<EmployeeVO> listEmployees(EmployeeQueryDTO query) {
        int page = query.getPage() != null ? query.getPage() : Constants.DEFAULT_PAGE;
        int size = Math.min(query.getSize() != null ? query.getSize() : Constants.DEFAULT_SIZE, Constants.MAX_SIZE);

        LambdaQueryWrapper<BizHrEmployee> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(BizHrEmployee::getRealName, query.getKeyword())
                    .or().like(BizHrEmployee::getEmployeeNo, query.getKeyword())
                    .or().like(BizHrEmployee::getPhone, query.getKeyword()));
        }
        if (query.getDeptId() != null) wrapper.eq(BizHrEmployee::getDeptId, query.getDeptId());
        if (query.getStatus() != null) wrapper.eq(BizHrEmployee::getStatus, query.getStatus());
        wrapper.orderByDesc(BizHrEmployee::getEntryDate);

        IPage<BizHrEmployee> pageResult = employeeMapper.selectPage(new Page<>(page, size), wrapper);
        List<EmployeeVO> voList = pageResult.getRecords().stream().map(this::toEmployeeVO).collect(Collectors.toList());
        return new PageResult<>(voList, pageResult.getTotal(), page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeVO getEmployeeById(Long id) {
        BizHrEmployee emp = employeeMapper.selectById(id);
        if (emp == null) throw new BusinessException(404, "员工不存在");
        return toEmployeeVO(emp);
    }

    @Override
    @Transactional
    public Long onboard(EmployeeCreateDTO dto) {
        // Create employee record
        BizHrEmployee emp = new BizHrEmployee();
        emp.setRealName(dto.getRealName());
        emp.setGender(dto.getGender());
        emp.setBirthDate(dto.getBirthDate());
        emp.setIdCard(dto.getIdCard());
        emp.setPhone(dto.getPhone());
        emp.setAddress(dto.getAddress());
        emp.setEntryDate(dto.getEntryDate());
        emp.setDeptId(dto.getDeptId());
        emp.setPosition(dto.getPosition());
        emp.setStatus(1);

        // Generate employee number
        String empNo = "EMP" + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMM"));
        LambdaQueryWrapper<BizHrEmployee> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.likeRight(BizHrEmployee::getEmployeeNo, empNo);
        long count = employeeMapper.selectCount(countWrapper);
        emp.setEmployeeNo(empNo + String.format("%03d", count + 1));

        // Auto-create system user if userId not specified
        if (dto.getUserId() != null) {
            emp.setUserId(dto.getUserId());
        } else {
            SysUser user = new SysUser();
            user.setUsername(emp.getEmployeeNo().toLowerCase());
            user.setRealName(dto.getRealName());
            user.setPhone(dto.getPhone());
            user.setDeptId(dto.getDeptId());
            user.setStatus(1);
            user.setPassword("$2a$10$defaultPasswordHash"); // Should be set properly
            userMapper.insert(user);
            emp.setUserId(user.getId());
        }

        employeeMapper.insert(emp);

        // Sync to contact book
        contactService.syncFromUser(emp.getUserId());

        log.info("Employee onboarded: {} ({})", emp.getEmployeeNo(), emp.getRealName());
        return emp.getId();
    }

    @Override
    @Transactional
    public void offboard(Long id) {
        BizHrEmployee emp = employeeMapper.selectById(id);
        if (emp == null) throw new BusinessException(404, "员工不存在");
        emp.setStatus(2);
        emp.setLeaveDate(LocalDate.now());
        employeeMapper.updateById(emp);

        // Disable system user
        if (emp.getUserId() != null) {
            SysUser user = userMapper.selectById(emp.getUserId());
            if (user != null) {
                user.setStatus(0);
                userMapper.updateById(user);
            }
            contactService.hideByUser(emp.getUserId());
        }

        log.info("Employee offboarded: {} ({})", emp.getEmployeeNo(), emp.getRealName());
    }

    // ==================== PAYROLL ====================

    @Override
    @Transactional(readOnly = true)
    public PageResult<PayrollVO> listPayrolls(String period, Integer status, Integer page, Integer size) {
        int p = page != null ? page : Constants.DEFAULT_PAGE;
        int s = Math.min(size != null ? size : Constants.DEFAULT_SIZE, Constants.MAX_SIZE);

        LambdaQueryWrapper<BizHrPayroll> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(period)) wrapper.eq(BizHrPayroll::getPeriod, period);
        if (status != null) wrapper.eq(BizHrPayroll::getStatus, status);
        wrapper.orderByDesc(BizHrPayroll::getCreatedAt);

        IPage<BizHrPayroll> pageResult = payrollMapper.selectPage(new Page<>(p, s), wrapper);
        List<PayrollVO> voList = pageResult.getRecords().stream().map(this::toPayrollVO).collect(Collectors.toList());
        return new PageResult<>(voList, pageResult.getTotal(), p, s);
    }

    @Override
    @Transactional
    public void generateMonthlyPayroll(String period) {
        // Check if already generated
        LambdaQueryWrapper<BizHrPayroll> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(BizHrPayroll::getPeriod, period);
        if (payrollMapper.selectCount(existWrapper) > 0) {
            throw new BusinessException("该月工资已生成");
        }

        // Get all active employees
        LambdaQueryWrapper<BizHrEmployee> empWrapper = new LambdaQueryWrapper<>();
        empWrapper.in(BizHrEmployee::getStatus, 1, 3);
        List<BizHrEmployee> employees = employeeMapper.selectList(empWrapper);

        for (BizHrEmployee emp : employees) {
            BizHrPayroll payroll = new BizHrPayroll();
            payroll.setEmployeeId(emp.getId());
            payroll.setPeriod(period);
            payroll.setBaseSalary(BigDecimal.ZERO);
            payroll.setOvertime(BigDecimal.ZERO);
            payroll.setBonus(BigDecimal.ZERO);
            payroll.setDeduction(BigDecimal.ZERO);
            payroll.setSocialInsurance(BigDecimal.ZERO);
            payroll.setTax(BigDecimal.ZERO);
            payroll.setNetSalary(BigDecimal.ZERO);
            payroll.setStatus(1); // pending adjustment
            payrollMapper.insert(payroll);
        }

        log.info("Monthly payroll generated for period {} ({} employees)", period, employees.size());
    }

    @Override
    @Transactional
    public void adjustPayroll(Long id, PayrollAdjustDTO dto) {
        BizHrPayroll payroll = payrollMapper.selectById(id);
        if (payroll == null) throw new BusinessException(404, "工资记录不存在");
        if (payroll.getStatus() != 1) throw new BusinessException("只有待调整状态可以修改");

        if (dto.getBonus() != null) payroll.setBonus(dto.getBonus());
        if (dto.getDeduction() != null) payroll.setDeduction(dto.getDeduction());

        // Recalculate tax and net salary
        BigDecimal gross = payroll.getBaseSalary()
                .add(payroll.getOvertime())
                .add(payroll.getBonus())
                .subtract(payroll.getDeduction());

        BigDecimal taxableIncome = gross.subtract(payroll.getSocialInsurance()).subtract(new BigDecimal("5000"));
        payroll.setTax(calculateTax(taxableIncome));
        payroll.setNetSalary(gross.subtract(payroll.getSocialInsurance()).subtract(payroll.getTax()));

        payroll.setStatus(2); // pending approval
        payrollMapper.updateById(payroll);
    }

    @Override
    @Transactional
    public void approvePayroll(Long id, String comment) {
        BizHrPayroll payroll = payrollMapper.selectById(id);
        if (payroll == null) throw new BusinessException(404, "工资记录不存在");
        if (comment == null || comment.length() < 2) throw new BusinessException("审批意见至少2个字符");

        if (payroll.getStatus() == 2) {
            payroll.setStatus(3); // Finance approved
            payrollMapper.updateById(payroll);
        } else if (payroll.getStatus() == 3) {
            payroll.setStatus(4); // GM approved
            payrollMapper.updateById(payroll);
        } else {
            throw new BusinessException("当前状态不支持审批");
        }
    }

    @Override
    @Transactional
    public void markPayrollPaid(Long id) {
        BizHrPayroll payroll = payrollMapper.selectById(id);
        if (payroll == null) throw new BusinessException(404, "工资记录不存在");
        if (payroll.getStatus() != 4) throw new BusinessException("只有已审批的工资可以标记已发放");
        payroll.setStatus(5);
        payroll.setPaidAt(LocalDateTime.now());
        payrollMapper.updateById(payroll);
    }

    // ==================== REIMBURSEMENT ====================

    @Override
    @Transactional(readOnly = true)
    public PageResult<ReimbursementVO> listReimbursements(Long employeeId, Integer status, Integer page, Integer size) {
        int p = page != null ? page : Constants.DEFAULT_PAGE;
        int s = Math.min(size != null ? size : Constants.DEFAULT_SIZE, Constants.MAX_SIZE);

        LambdaQueryWrapper<BizHrReimbursement> wrapper = new LambdaQueryWrapper<>();
        if (employeeId != null) wrapper.eq(BizHrReimbursement::getEmployeeId, employeeId);
        if (status != null) wrapper.eq(BizHrReimbursement::getStatus, status);
        wrapper.orderByDesc(BizHrReimbursement::getCreatedAt);

        IPage<BizHrReimbursement> pageResult = reimbursementMapper.selectPage(new Page<>(p, s), wrapper);
        List<ReimbursementVO> voList = pageResult.getRecords().stream().map(this::toReimbursementVO).collect(Collectors.toList());
        return new PageResult<>(voList, pageResult.getTotal(), p, s);
    }

    @Override
    @Transactional
    public Long createReimbursement(ReimbursementCreateDTO dto) {
        // Validate category (6 types)
        List<String> validCategories = List.of("交通费", "餐饮费", "住宿费", "办公用品", "通讯费", "其他");
        if (!validCategories.contains(dto.getCategory())) {
            throw new BusinessException("报销类别无效，允许: " + String.join("/", validCategories));
        }

        BizHrReimbursement reimb = new BizHrReimbursement();
        reimb.setEmployeeId(dto.getEmployeeId());
        reimb.setProjectId(dto.getProjectId());
        reimb.setReimburseNo(bizNoGenerator.generateReimburseNo());
        reimb.setCategory(dto.getCategory());
        reimb.setAmount(dto.getAmount());
        reimb.setDescription(dto.getDescription());
        reimb.setFileUrl(dto.getFileUrl());
        reimb.setStatus(2); // Submit directly to supervisor
        reimb.setCreatorId(SecurityUtils.getCurrentUserId());
        reimbursementMapper.insert(reimb);

        log.info("Reimbursement created: {}", reimb.getReimburseNo());
        return reimb.getId();
    }

    @Override
    @Transactional
    public void approveReimbursement(Long id, String comment) {
        BizHrReimbursement reimb = reimbursementMapper.selectById(id);
        if (reimb == null) throw new BusinessException(404, "报销单不存在");
        if (comment == null || comment.length() < 2) throw new BusinessException("审批意见至少2个字符");

        if (reimb.getStatus() == 2) {
            reimb.setStatus(3); // Supervisor -> Finance
            reimbursementMapper.updateById(reimb);
        } else if (reimb.getStatus() == 3) {
            reimb.setStatus(4); // Finance -> Cashier
            reimbursementMapper.updateById(reimb);
        } else if (reimb.getStatus() == 4) {
            reimb.setStatus(5); // Cashier confirm -> completed
            reimb.setApproverId(SecurityUtils.getCurrentUserId());
            reimb.setApprovedAt(LocalDateTime.now());
            reimbursementMapper.updateById(reimb);
        } else {
            throw new BusinessException("当前状态不支持审批");
        }
    }

    @Override
    @Transactional
    public void rejectReimbursement(Long id, String comment) {
        BizHrReimbursement reimb = reimbursementMapper.selectById(id);
        if (reimb == null) throw new BusinessException(404, "报销单不存在");
        if (reimb.getStatus() < 2 || reimb.getStatus() > 4) throw new BusinessException("当前状态不支持驳回");
        if (comment == null || comment.length() < 5) throw new BusinessException("驳回意见至少5个字符");
        reimb.setStatus(6);
        reimbursementMapper.updateById(reimb);
    }

    // ==================== CONTRACT ====================

    @Override
    @Transactional(readOnly = true)
    public List<HrContractVO> listContracts(Long employeeId) {
        LambdaQueryWrapper<BizHrContract> wrapper = new LambdaQueryWrapper<>();
        if (employeeId != null) wrapper.eq(BizHrContract::getEmployeeId, employeeId);
        wrapper.orderByDesc(BizHrContract::getStartDate);
        return hrContractMapper.selectList(wrapper).stream().map(this::toContractVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Long createContract(HrContractCreateDTO dto) {
        BizHrContract contract = new BizHrContract();
        contract.setEmployeeId(dto.getEmployeeId());
        contract.setContractType(dto.getContractType());
        contract.setStartDate(dto.getStartDate());
        contract.setEndDate(dto.getEndDate());
        contract.setFileUrl(dto.getFileUrl());
        contract.setStatus(1);
        hrContractMapper.insert(contract);
        return contract.getId();
    }

    @Override
    @Transactional
    public void renewContract(Long id, HrContractCreateDTO dto) {
        BizHrContract old = hrContractMapper.selectById(id);
        if (old == null) throw new BusinessException(404, "合同不存在");
        old.setStatus(3); // renewed
        hrContractMapper.updateById(old);

        BizHrContract newContract = new BizHrContract();
        newContract.setEmployeeId(old.getEmployeeId());
        newContract.setContractType(dto.getContractType() != null ? dto.getContractType() : old.getContractType());
        newContract.setStartDate(dto.getStartDate());
        newContract.setEndDate(dto.getEndDate());
        newContract.setFileUrl(dto.getFileUrl());
        newContract.setStatus(1);
        hrContractMapper.insert(newContract);

        log.info("Contract renewed for employee {}", old.getEmployeeId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HrContractVO> getExpiringContracts(int days) {
        LocalDate deadline = LocalDate.now().plusDays(days);
        LambdaQueryWrapper<BizHrContract> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizHrContract::getStatus, 1)
                .le(BizHrContract::getEndDate, deadline)
                .ge(BizHrContract::getEndDate, LocalDate.now());
        return hrContractMapper.selectList(wrapper).stream().map(this::toContractVO).collect(Collectors.toList());
    }

    // ==================== QUALIFICATION ====================

    @Override
    @Transactional(readOnly = true)
    public List<QualificationVO> listQualifications(Long employeeId) {
        LambdaQueryWrapper<BizHrQualification> wrapper = new LambdaQueryWrapper<>();
        if (employeeId != null) wrapper.eq(BizHrQualification::getEmployeeId, employeeId);
        wrapper.orderByAsc(BizHrQualification::getExpireDate);
        return qualificationMapper.selectList(wrapper).stream().map(this::toQualVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Long createQualification(Long employeeId, String qualName, String qualNo, String issueDate, String expireDate, String fileUrl) {
        BizHrQualification qual = new BizHrQualification();
        qual.setEmployeeId(employeeId);
        qual.setQualName(qualName);
        qual.setQualNo(qualNo);
        if (issueDate != null) qual.setIssueDate(LocalDate.parse(issueDate));
        if (expireDate != null) qual.setExpireDate(LocalDate.parse(expireDate));
        qual.setFileUrl(fileUrl);
        qualificationMapper.insert(qual);
        return qual.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QualificationVO> getExpiringQualifications(int days) {
        LocalDate deadline = LocalDate.now().plusDays(days);
        LambdaQueryWrapper<BizHrQualification> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(BizHrQualification::getExpireDate, deadline)
                .ge(BizHrQualification::getExpireDate, LocalDate.now());
        return qualificationMapper.selectList(wrapper).stream().map(this::toQualVO).collect(Collectors.toList());
    }

    // ==================== TAX CALCULATION ====================

    private BigDecimal calculateTax(BigDecimal taxableIncome) {
        if (taxableIncome.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        BigDecimal tax;
        BigDecimal income = taxableIncome;
        // China individual income tax brackets (monthly)
        if (income.compareTo(new BigDecimal("3000")) <= 0) {
            tax = income.multiply(new BigDecimal("0.03"));
        } else if (income.compareTo(new BigDecimal("12000")) <= 0) {
            tax = income.multiply(new BigDecimal("0.10")).subtract(new BigDecimal("210"));
        } else if (income.compareTo(new BigDecimal("25000")) <= 0) {
            tax = income.multiply(new BigDecimal("0.20")).subtract(new BigDecimal("1410"));
        } else if (income.compareTo(new BigDecimal("35000")) <= 0) {
            tax = income.multiply(new BigDecimal("0.25")).subtract(new BigDecimal("2660"));
        } else if (income.compareTo(new BigDecimal("55000")) <= 0) {
            tax = income.multiply(new BigDecimal("0.30")).subtract(new BigDecimal("4410"));
        } else if (income.compareTo(new BigDecimal("80000")) <= 0) {
            tax = income.multiply(new BigDecimal("0.35")).subtract(new BigDecimal("7160"));
        } else {
            tax = income.multiply(new BigDecimal("0.45")).subtract(new BigDecimal("15160"));
        }
        return tax.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    // ==================== VO CONVERTERS ====================

    private EmployeeVO toEmployeeVO(BizHrEmployee emp) {
        EmployeeVO vo = new EmployeeVO();
        vo.setId(emp.getId());
        vo.setUserId(emp.getUserId());
        vo.setEmployeeNo(emp.getEmployeeNo());
        vo.setRealName(emp.getRealName());
        vo.setGender(emp.getGender());
        vo.setGenderName(emp.getGender() != null ? (emp.getGender() == 1 ? "男" : "女") : null);
        vo.setBirthDate(emp.getBirthDate());
        vo.setIdCard(emp.getIdCard());
        vo.setPhone(emp.getPhone());
        vo.setAddress(emp.getAddress());
        vo.setEntryDate(emp.getEntryDate());
        vo.setLeaveDate(emp.getLeaveDate());
        vo.setStatus(emp.getStatus());
        vo.setStatusName(EMPLOYEE_STATUS.getOrDefault(emp.getStatus(), "未知"));
        vo.setDeptId(emp.getDeptId());
        vo.setPosition(emp.getPosition());
        vo.setCreatedAt(emp.getCreatedAt());
        if (emp.getDeptId() != null) {
            SysDepartment dept = departmentMapper.selectById(emp.getDeptId());
            if (dept != null) vo.setDeptName(dept.getDeptName());
        }
        return vo;
    }

    private PayrollVO toPayrollVO(BizHrPayroll p) {
        PayrollVO vo = new PayrollVO();
        vo.setId(p.getId());
        vo.setEmployeeId(p.getEmployeeId());
        vo.setPeriod(p.getPeriod());
        vo.setBaseSalary(p.getBaseSalary());
        vo.setOvertime(p.getOvertime());
        vo.setBonus(p.getBonus());
        vo.setDeduction(p.getDeduction());
        vo.setSocialInsurance(p.getSocialInsurance());
        vo.setTax(p.getTax());
        vo.setNetSalary(p.getNetSalary());
        vo.setStatus(p.getStatus());
        vo.setStatusName(PAYROLL_STATUS.getOrDefault(p.getStatus(), "未知"));
        vo.setPaidAt(p.getPaidAt());
        vo.setCreatedAt(p.getCreatedAt());
        if (p.getEmployeeId() != null) {
            BizHrEmployee emp = employeeMapper.selectById(p.getEmployeeId());
            if (emp != null) vo.setEmployeeName(emp.getRealName());
        }
        return vo;
    }

    private ReimbursementVO toReimbursementVO(BizHrReimbursement r) {
        ReimbursementVO vo = new ReimbursementVO();
        vo.setId(r.getId());
        vo.setEmployeeId(r.getEmployeeId());
        vo.setProjectId(r.getProjectId());
        vo.setReimburseNo(r.getReimburseNo());
        vo.setCategory(r.getCategory());
        vo.setAmount(r.getAmount());
        vo.setDescription(r.getDescription());
        vo.setStatus(r.getStatus());
        vo.setStatusName(REIMBURSE_STATUS.getOrDefault(r.getStatus(), "未知"));
        vo.setFileUrl(r.getFileUrl());
        vo.setApprovedAt(r.getApprovedAt());
        vo.setCreatedAt(r.getCreatedAt());
        if (r.getEmployeeId() != null) {
            BizHrEmployee emp = employeeMapper.selectById(r.getEmployeeId());
            if (emp != null) vo.setEmployeeName(emp.getRealName());
        }
        if (r.getProjectId() != null) {
            BizProject project = projectMapper.selectById(r.getProjectId());
            if (project != null) vo.setProjectName(project.getProjectName());
        }
        return vo;
    }

    private HrContractVO toContractVO(BizHrContract c) {
        HrContractVO vo = new HrContractVO();
        vo.setId(c.getId());
        vo.setEmployeeId(c.getEmployeeId());
        vo.setContractType(c.getContractType());
        vo.setContractTypeName(CONTRACT_TYPE_MAP.getOrDefault(c.getContractType(), "未知"));
        vo.setStartDate(c.getStartDate());
        vo.setEndDate(c.getEndDate());
        vo.setFileUrl(c.getFileUrl());
        vo.setStatus(c.getStatus());
        vo.setStatusName(CONTRACT_STATUS.getOrDefault(c.getStatus(), "未知"));
        if (c.getEndDate() != null) {
            vo.setDaysToExpire(ChronoUnit.DAYS.between(LocalDate.now(), c.getEndDate()));
        }
        if (c.getEmployeeId() != null) {
            BizHrEmployee emp = employeeMapper.selectById(c.getEmployeeId());
            if (emp != null) vo.setEmployeeName(emp.getRealName());
        }
        return vo;
    }

    private QualificationVO toQualVO(BizHrQualification q) {
        QualificationVO vo = new QualificationVO();
        vo.setId(q.getId());
        vo.setEmployeeId(q.getEmployeeId());
        vo.setQualName(q.getQualName());
        vo.setQualNo(q.getQualNo());
        vo.setIssueDate(q.getIssueDate());
        vo.setExpireDate(q.getExpireDate());
        vo.setFileUrl(q.getFileUrl());
        if (q.getExpireDate() != null) {
            vo.setDaysToExpire(ChronoUnit.DAYS.between(LocalDate.now(), q.getExpireDate()));
        }
        if (q.getEmployeeId() != null) {
            BizHrEmployee emp = employeeMapper.selectById(q.getEmployeeId());
            if (emp != null) vo.setEmployeeName(emp.getRealName());
        }
        return vo;
    }
}
