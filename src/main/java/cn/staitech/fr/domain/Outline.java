package cn.staitech.fr.domain;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * (Outline)表实体类
 *
 * @author wangfeng
 * @since 2024-01-04 10:55:03
 */
@Data
@TableName("tb_outline")
public class Outline extends Model<Outline> {
    @TableId(value = "outline_id", type = IdType.AUTO)
    @ApiModelProperty(value = "标注ID")
    private Long outlineId;
    @ApiModelProperty(value = "项目ID")
    private Long projectId;
    @ApiModelProperty(value = "图像ID")
    private Long imageId;
    @ApiModelProperty(value = "切片ID")
    private Long slideId;
    @ApiModelProperty(value = "创建者ID")
    private Long createBy;
    @ApiModelProperty(value = "面积")
    private Double area;
    @ApiModelProperty(value = "周长")
    private Double perimeter;
    @ApiModelProperty(value = "长轴")
    private Double longAxis;
    @ApiModelProperty(value = "短轴")
    private Double shortAxis;

    @ApiModelProperty(value = "标注数据")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private JSONObject geometry;
    @ApiModelProperty(value = "当前用户登录token ID")
    private String token;
}

