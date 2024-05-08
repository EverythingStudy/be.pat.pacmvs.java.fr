package cn.staitech.fr.domain.out;


import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SpeciesOut {

    /**
     * 种属key
     */
    @ApiModelProperty(value = "种属ID")
    private String speciesId;

    /**
     * 种属名称
     */
    @ApiModelProperty(value = "种属名称")
    private String name;

    /**
     * 种属名称EN
     */
    @ApiModelProperty(value = "种属名称En")
    private String nameEn;

    /**
     * 机构ID
     */
    @ApiModelProperty(value = "机构ID")
    private Long organizationId;




}
