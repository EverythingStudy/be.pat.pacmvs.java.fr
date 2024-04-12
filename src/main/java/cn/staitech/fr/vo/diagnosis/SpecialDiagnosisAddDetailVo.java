package cn.staitech.fr.vo.diagnosis;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpecialDiagnosisAddDetailVo {
	@ApiModelProperty(name = "remark", value = "备注")
	private String remark;

	@ApiModelProperty(name = "diagnosisStatus", value = "诊断状态 1：未诊断 1：已诊断")
	private Integer diagnosisStatus;

	@ApiModelProperty(name = "status", value = "是否可用 0:不可用1:可用")
	private Integer status;

	@ApiModelProperty(name = "deleteFlag", value = "逻辑删除状态（0:删除 1:未删除）")
	private Integer deleteFlag;

	@ApiModelProperty(name = "createBy", value = "创建人id")
	private Long createBy;

	@ApiModelProperty(name = "createTime", value = "创建时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	@ApiModelProperty(name = "updateBy", value = "更新人id")
	private Long updateBy;

	@ApiModelProperty(name = "updateTime", value = "更新时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;
	
	@ApiModelProperty(name = "viscera", value = "viscera列表")
	private List<Object> visceraList = new ArrayList<Object>();

	@ApiModelProperty(name = "position", value = "position列表")
	private List<Object> positionList = new ArrayList<Object>();

	@ApiModelProperty(name = "lesion", value = "lesion列表")
	private List<Object> lesionList = new ArrayList<Object>();

	@ApiModelProperty(name = "ddefinition", value = "ddefinition列表")
	private List<Object> ddefinitionList = new ArrayList<Object>();

	@ApiModelProperty(name = "grade", value = "grade列表")
	private List<Object> gradeList = new ArrayList<Object>();

	@ApiModelProperty(name = "editStatus", value = "是否可以编辑 0:不可以1:可以")
	private Integer editStatus = 0;

	@ApiModelProperty(name = "diagnosticResults", value = "诊断结果")
	private String diagnosticResults;

	@ApiModelProperty(name = "positionWord", value = "position自定义")
	private String positionWord;
	
	@ApiModelProperty(name = "visceraWord", value = "viscera自定义")
	private String visceraWord;

	@ApiModelProperty(name = "lesionWord", value = "lesion自定义")
	private String lesionWord;

	@ApiModelProperty(name = "ddefinitionWord", value = "ddefinition自定义")
	private String ddefinitionWord;

	@ApiModelProperty(name = "gradeWord", value = "grade自定义")
	private String gradeWord;

}
