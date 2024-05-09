package cn.staitech.fr.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import cn.staitech.fr.domain.Diagnosis;
import cn.staitech.fr.domain.out.ExportListVO;
import cn.staitech.system.api.domain.SysUser;

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

	List<ExportListVO>  getExportListVO(Long singleId);

	String getOrganizationName(Long organizationId);
	
	List<Diagnosis> selectListByParm(Diagnosis diagnosis);
}
