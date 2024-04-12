package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.Diagnosis;
import cn.staitech.system.api.domain.SysUser;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 人工诊断表 Mapper 接口
 * </p>
 *
 * @author wanglibei
 * @since 2024-04-11
 */
public interface DiagnosisMapper extends BaseMapper<Diagnosis> {
	SysUser selectUserById(Long userId);
}
