package cn.staitech.fr.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import cn.staitech.fr.domain.Diagnosis;
import cn.staitech.fr.vo.diagnosis.SpecialDiagnosisAddVo;
import cn.staitech.fr.vo.diagnosis.SpecialDiagnosisVo;
import cn.staitech.fr.vo.diagnosis.SysDictDataVo;
import cn.staitech.fr.vo.diagnosis.SysDictResultVo;
import cn.staitech.fr.vo.diagnosis.VisceraVo;

/**
 * <p>
 * 人工诊断表 服务类
 * </p>
 *
 * @author wanglibei
 * @since 2024-04-11
 */
public interface DiagnosisService extends IService<Diagnosis> {
	
	//查询诊断数据列表
	public List<SpecialDiagnosisVo> getSpecialDiagnosisVo(Long singleId);
	//保存/修改诊断数据
	public int saveOrUpdateSpecialDiagnosisVo(SpecialDiagnosisAddVo addVoList);
	
	public List<SysDictDataVo> getCommonTag(String dictType);
	
	public List<VisceraVo> getRelationshipTag(String dictType);

	
	public void deleteSpecialDiagnosisVo( Long specialDiagnosisId);
	
	public Diagnosis getSpecialDiagnosis(Long specialDiagnosisId); 
	
	public SysDictResultVo getSysDictResultVo(); 

}
