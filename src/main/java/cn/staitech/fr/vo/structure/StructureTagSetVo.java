package cn.staitech.fr.vo.structure;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author mugw
 * @version 1.0
 * @description
 * @date 2025/5/30 17:20:15
 */
@Data
public class StructureTagSetVo {


    /**
     * 物种ID
     */
    private String speciesId;

    /**
     * 器官名称
     */
    private String organName;

    private String organNameEn;

    /**
     * 器官ID
     */
    @JsonProperty("organId")
    private String organCode;

    /**
     * 物种名称
     */
    private String speciesName;

    private String speciesNameEn;

    /**
     * 指标类型 (1:组织学, 2:分子)
     */
    @JsonProperty("indicatorType")
    private Integer type = 0;

    @JsonProperty("indicatorName")
    private String structureTagSetName;

    @JsonProperty("indicatorNameEn")
    private String structureTagSetNameEn;

    /**
     * 指标ID（前端传参为 indicatorId）
     */
    @JsonProperty("indicatorId")
    private Long structureTagSetId;

    @JsonProperty("userName")
    private String createName;

    @JsonProperty("annotationCategoryTotal")
    private Integer tags;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

}
