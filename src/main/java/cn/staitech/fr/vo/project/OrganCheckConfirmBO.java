package cn.staitech.fr.vo.project;

import lombok.Data;

import java.io.Serializable;

/**
 * 脏器识别校对-确认修改
 *
 * @author yxy
 */
@Data
public class OrganCheckConfirmBO implements Serializable {
    /**
     * 图像ID
     */
    private Long imageId;
    /**
     * 图像的绝对路径
     */
    private String imagePath;
    /**
     * 切片ID
     */
    private Long slideId;
    /**
     * 单脏器切片id
     */
    private Long singleId;
    /**
     * 脏器标签ID
     */
    private Long categoryId;
    /**
     * 机构ID
     */
    private Long organizationId;
    /**
     * 机构名称
     */
    private String organizationName;
    /**
     * 专题名称
     */
    private String topicName;
}
