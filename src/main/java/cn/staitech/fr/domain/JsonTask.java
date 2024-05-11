package cn.staitech.fr.domain;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * @author: wangfeng
 * @create: 2024-05-10 14:16:36
 * @Description: (JsonTask)表实体类
 */

@Data
@TableName(value = "fr_json_task")
@SuppressWarnings("serial")
public class JsonTask implements Serializable {
    //任务ID
    private Long taskId;
    //项目ID
    private Long projectId;
    //切片ID
    private Long slideId;
    //专题ID
    private Integer specialId;
    //图像ID
    private Integer imageId;
    //单脏器切片id
    private Long singleId;
    //机构ID
    private Long organizationId;
    //脏器标签ID
    private Long categoryId;
    //算法ID
    private Integer algorithmUuid;
    //算法code
    private String code;
    //JSON文件路径
    private String jsonPath;
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

    @TableField(exist = false)
    private String algorithmName;

    @TableField(exist = false)
    //private String fileUrlList;
    private String[] fileUrlList;
}

