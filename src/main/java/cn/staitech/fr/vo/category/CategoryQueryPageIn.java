package cn.staitech.fr.vo.category;

import cn.staitech.common.core.domain.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

@Data
public class CategoryQueryPageIn extends PageRequest {

    @ApiModelProperty(value = "种属")
    private String species;

    @ApiModelProperty(value = "脏器名称")
    private String organName;

    @ApiModelProperty(value = "简称")
    private String categoryAbbreviation;

    @ApiModelProperty(name ="createTime",value = "请求参数时间")
    private Map<String, Object> createTime;
}
