package cn.staitech.fr.vo.project;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author wudi
 * @Date 2024/3/29 16:22
 * @desc
 */
@Data
public class ProjectStatusVo {
    @ApiModelProperty(required = true, value = "项目id")
    private Long projectId;
    /**
     * 以下属性暂未使用
     */
    @ApiModelProperty( value = "状态:1-启动，2-暂停，3-完成，6-归档")
    private Integer status;
    
    @ApiModelProperty(value = "用户名")
    private String userName;
    
    @ApiModelProperty(value = "密码")
    private String pwd;
    
    @ApiModelProperty(value = "原因")
    private String reason;
}
