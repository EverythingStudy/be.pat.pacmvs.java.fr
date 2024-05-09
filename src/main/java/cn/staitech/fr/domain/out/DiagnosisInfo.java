package cn.staitech.fr.domain.out;

import java.util.List;

import cn.staitech.fr.vo.diagnosis.SpecialDiagnosisVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DiagnosisInfo {
	@ApiModelProperty(name = "list" , value = "诊断列表")
	List<SpecialDiagnosisVo> list;
	@ApiModelProperty(name = "abnormalStatus" , value = "异常状态 0：默认值 ；1：未见异常")
	private String abnormalStatus;
}
