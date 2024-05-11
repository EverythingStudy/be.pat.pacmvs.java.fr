package cn.staitech.fr.domain;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * (JsonFile)表实体类
 *
 * @author makejava
 * @since 2024-05-11 13:56:32
 */
@Data
@TableName(value = "fr_json_file")
@SuppressWarnings("serial")
public class JsonFile extends Model<JsonFile> {
    //文件ID
    @ApiModelProperty(value = "文件ID")
    @TableId(value = "file_id", type = IdType.AUTO)
    private Long fileId;
    //任务ID
    private Long taskId;
    @ApiModelProperty(value = "结构标识")
    private String structureName;
    //文件路径
    private String fileUrl;

    //状态(0未进行解析、1解析中、2解析成功、3解析失败)
    private Integer status;
    //执行次数（第几次）
    private Integer times;
    //开始时间
    private Date startTime;
    //结束时间
    private Date endTime;
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;
}

