package cn.staitech.fr.vo.annotation.in;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class MarkingMerge {

    @ApiModelProperty(value = "id集合", required = true)
    private List<Long> markingIdList;

    @ApiModelProperty(value = "切片id", required = true)
    private Long slideId;

    private Long sequenceNumber;

    @ApiModelProperty(value = "项目id", required = true)
    private Long project_id;



}
