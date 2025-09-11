package cn.staitech.fr.vo.project;

import cn.staitech.common.core.domain.DateRangeReq;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class SlideDownLoadReq {
    /**
     * 项目id
     */
    @ApiModelProperty(value = "项目id-V2.6.1", required = true)
    @NotNull(message = "项目编号不能为空")
    private Long projectId;
    /**
     * 切片编号
     */
    @ApiModelProperty(value = "切片编号-V2.6.1")
    private String fileName;
    /**
     * 动物编号集合
     */
    @ApiModelProperty(value = "动物编号集合-V2.6.1")
    private List<String> animalCodes;
    /**
     * 蜡块编号集合
     */
    @ApiModelProperty(value = "蜡块编号集合-V2.6.1")
    private List<String> waxCodes;
    /**
     * 组号集合
     */
    @ApiModelProperty(value = "组号集合-V2.6.1")
    private List<String> groupCodes;
    /**
     * 性别
     */
    @ApiModelProperty(value = "性别，单选F、M-V2.6.1")
    private String genderFlag;
    /**
     * 脏器标签ID集合
     */
    @ApiModelProperty(value = "脏器标签ID集合-V2.6.1")
    private List<Long> organTagIds;
    /**
     * 阅片状态集合
     */
    @ApiModelProperty(value = "阅片状态集合：0-未阅片；1-已阅片-V2.6.1")
    private Integer viewStatus;
    /**
     * AI分析状态：0-未分析、1-脏器识别中、2-脏器识别异常、3-脏器识别完成、4-结构未分析、5-结构分析中、6-结构分析完成、7-结构分析失败
     */
    @ApiModelProperty(value = "AI分析状态：0-未分析、1-脏器识别中、2-脏器识别异常、4-结构未分析、5-结构分析中、6-结构分析完成、7-结构分析失败-V2.6.1")
    private List<Integer> aiStatus;
    /**
     * 描述
     */
    @ApiModelProperty(value = "描述-V2.6.1")
    private String description;
    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间-V2.6.1")
    private DateRangeReq createTimeParams;

    @ApiModelProperty(value = "选片id集合")
    @NotNull(message = "选片编号不能为空")
    private List<Long> slideIds;
    @ApiModelProperty(value = "切片id集合")
    @NotNull(message = "切片编号不能为空")
    private List<Long> singleSlideIds;
}
