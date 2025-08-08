package cn.staitech.fr.domain;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class OrganizationIdName implements Serializable {

    /**
     * 机构ID
     */
    @ApiModelProperty(value = "机构ID")
    private Long organizationId;


    /**
     * 机构名称
     */
    @ApiModelProperty(value = "机构名称")
    private String organizationName;
}
