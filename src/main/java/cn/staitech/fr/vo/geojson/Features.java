package cn.staitech.fr.vo.geojson;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class Features {

    @ApiModelProperty(value = "id")
    private String id;

    @ApiModelProperty(value = "type")
    private String type = "Feature";

    @TableField(typeHandler = JacksonTypeHandler.class)
    @ApiModelProperty(value = "标注坐标")
    private JSONObject geometry;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @ApiModelProperty(value = "Properties")
    private JSONObject properties;
}
