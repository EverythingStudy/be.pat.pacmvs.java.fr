package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 切片-专题（原图像）表 tb_topic
 *
 * @author WangFeng
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "tb_topic")
public class Topic implements Serializable {


    /**
     * 主键id .
     */
    @TableId(value = "topic_id", type = IdType.AUTO)
    private Long topicId;

    /**
     * 专题名称 .
     */
    @TableField(value = "topic_name")
    private String topicName;

    /**
     * 组织ID .
     */
    @TableField(value = "organization_id")
    private Long organizationId;

    /**
     * 项目类型ID .
     */
    @TableField(value = "project_type_id")
    private Integer projectTypeId;

    /**
     * 创建者 .
     */
    @TableField(value = "create_by")
    private Long createBy;

    /**
     * 创建时间 .
     */
    @TableField(value = "create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String createTime;

    /**
     * 更新者 .
     */
    @TableField(value = "update_by")
    private Long updateBy;

    /**
     * 更新时间 .
     */
    @TableField(value = "update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String updateTime;
}
