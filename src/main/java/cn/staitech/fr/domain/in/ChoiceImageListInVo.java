package cn.staitech.fr.domain.in;

import cn.staitech.common.core.domain.PageRequest;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @Author wudi
 * @Date 2024/3/7 10:56
 * @desc
 */
@Data
public class ChoiceImageListInVo extends PageRequest {
    @Size(min = 0, max = 200, message = "{ImageTopicVO.imageName.length}")
    @ApiModelProperty(value = "文件名称-切片编号")
    private String imageName;
    @ApiModelProperty(value = "添加状态：NULL查全部、0未添加、1已添加")
    private Integer choiceState;
    @ApiModelProperty(hidden = true)
    private Long orgId;
    @ApiModelProperty(value = "专题id")
    private Long specialId;

}
