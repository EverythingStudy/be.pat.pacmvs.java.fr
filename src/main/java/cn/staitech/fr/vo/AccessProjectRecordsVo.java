package cn.staitech.fr.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author mugw
 * @version 2.6.0
 * @description 近一个月访问记录
 * @date 2025/5/14 13:44:14
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AccessProjectRecordsVo {

    @ApiModelProperty(value = "访问数量")
   private Integer num;

    @ApiModelProperty(value = "访问时间")
    private String accessTime;
}
