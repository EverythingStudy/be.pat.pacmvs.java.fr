package cn.staitech.fr.domain;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 切片-项目（原图像）表 tb_topic
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
     * 项目名称 .
     */
    @TableField(value = "topic_name")
    private String topicName;

    /**
     * 组织ID .
     */
    @TableField(value = "organization_id")
    private Long organizationId;


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
