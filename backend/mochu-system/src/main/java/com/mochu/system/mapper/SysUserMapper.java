package com.mochu.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mochu.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    @Select("SELECT * FROM sys_user WHERE (username = #{account} OR phone = #{account}) AND deleted = 0 LIMIT 1")
    SysUser selectByAccount(@Param("account") String account);
}
