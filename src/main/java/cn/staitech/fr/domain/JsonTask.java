package cn.staitech.fr.domain;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * @author: wangfeng
 * @create: 2024-05-10 14:16:36
 * @Description: (JsonTask)表实体类
 * <p>
 * MQ Message
 */

@Data
@TableName(value = "fr_json_task")
@SuppressWarnings("serial")
public class JsonTask implements Serializable {

    @ApiModelProperty(value = "任务ID")
    @TableId(value = "task_id", type = IdType.AUTO)
    private Long taskId;
    //切片ID
    private Long slideId;
    //专题ID
    private Long specialId;
    //图像ID
    private Long imageId;
    //单脏器切片id
    private Long singleId;
    //机构ID
    private Long organizationId;
    //脏器标签ID
    private Long categoryId;
    //算法名称标识
    private String algorithmCode;
    // 状态码
    private String code;
    // msg
    private String msg;
    // data内容
    private String data;
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
    //结构化时间
    private Long structureTime;

}

