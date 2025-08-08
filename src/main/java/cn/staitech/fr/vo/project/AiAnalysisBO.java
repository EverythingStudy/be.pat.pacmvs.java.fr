package cn.staitech.fr.vo.project;

import lombok.Data;

import java.io.Serializable;

/**
 * AI分析
 *
 * @author yxy
 */
@Data
public class AiAnalysisBO implements Serializable {
    /**
     * 切片ID
     */
    private Long slideId;
    /**
     * 图像ID
     */
    private Long imageId;
    /**
     * 图像的绝对路径
     */
    private String imagePath;
    /**
     * 无扩展名文件名称
     */
    private String fileName;
    /**
     * 专题名称
     */
    private String topicName;
    /**
     * 机构ID
     */
    private Long organizationId;
    /**
     * 机构名称
     */
    private String organizationName;
}
