/**
 * @filename:SysDictData 1687870937012
 * @project business  V1.0
 * Copyright(c) 2020 wanglibei Co. Ltd. 
 * All right reserved. 
 */
package cn.staitech.fr.domain;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.util.Date;

/**
 * 
* @ClassName: SysDictData
* @Description:
* @author wanglibei
* @date 2023年6月27日
* @version V1.0
 */
@Api(value = "系统字典", tags = "系统字典")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysDictData{

	
	@ApiModelProperty(name = "dictCode" , value = "字典编码")
	private Long dictCode;
    
	@ApiModelProperty(name = "dictSort" , value = "字典排序")
	private Integer dictSort;
    
	@ApiModelProperty(name = "dictLabel" , value = "字典标签")
	private String dictLabel;
    
	@ApiModelProperty(name = "dictLabelEn" , value = "字典标签(英语)")
	private String dictLabelEn;
    
	@ApiModelProperty(name = "dictValue" , value = "字典键值")
	private String dictValue;
    
	@ApiModelProperty(name = "dictType" , value = "字典类型")
	private String dictType;
    
	@ApiModelProperty(name = "cssClass" , value = "样式属性（其他样式扩展）")
	private String cssClass;
    
	@ApiModelProperty(name = "listClass" , value = "表格回显样式")
	private String listClass;
    
	@ApiModelProperty(name = "isDefault" , value = "是否默认（Y是 N否）")
	private String isDefault;
    
	@ApiModelProperty(name = "status" , value = "状态（0正常 1停用）")
	private String status;
    
	@ApiModelProperty(name = "createBy" , value = "创建者")
	private Long createBy;
    
	@ApiModelProperty(name = "createTime" , value = "创建时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
    
	@ApiModelProperty(name = "updateBy" , value = "更新者")
	private Long updateBy;
    
	@ApiModelProperty(name = "updateTime" , value = "更新时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;
    
	@ApiModelProperty(name = "remark" , value = "备注")
	private String remark;
    
	@ApiModelProperty(name = "filter" , value = "过滤条件")
	private String filter;
    
	@ApiModelProperty(name = "color" , value = "色值 如：rgba(217,128,95,1)")
	private String color;

}
