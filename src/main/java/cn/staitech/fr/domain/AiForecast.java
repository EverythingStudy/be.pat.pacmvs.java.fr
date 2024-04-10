package cn.staitech.fr.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * @TableName fr_ai_forecast
 */
@TableName(value ="fr_ai_forecast")
@Data
public class AiForecast implements Serializable {
    /**
     * 
     */
    @TableId
    private Integer forecastId;

    /**
     * 单脏器切片id
     */
    @ApiModelProperty(name = "singleSlideId" , value = "单脏器切片id")
    private Integer singleSlideId;

    /**
     * 定量指标
     */
    @ApiModelProperty(name = "quantitativeIndicators" , value = "定量指标")
    private String quantitativeIndicators;

    /**
     * 预测结果
     */
    @ApiModelProperty(name = "results" , value = "预测结果")
    private String results;

    /**
     * 范围
     */
    @ApiModelProperty(name = "range" , value = "范围")
    private String range;

    /**
     * 创建者
     */
    @ApiModelProperty(name = "createBy" , value = "创建者")
    private Long createBy;

    /**
     * 创建时间
     */
    @ApiModelProperty(name = "createTime" , value = "创建时间")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        AiForecast other = (AiForecast) that;
        return (this.getForecastId() == null ? other.getForecastId() == null : this.getForecastId().equals(other.getForecastId()))
            && (this.getQuantitativeIndicators() == null ? other.getQuantitativeIndicators() == null : this.getQuantitativeIndicators().equals(other.getQuantitativeIndicators()))
            && (this.getResults() == null ? other.getResults() == null : this.getResults().equals(other.getResults()))
            && (this.getRange() == null ? other.getRange() == null : this.getRange().equals(other.getRange()))
            && (this.getCreateBy() == null ? other.getCreateBy() == null : this.getCreateBy().equals(other.getCreateBy()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getForecastId() == null) ? 0 : getForecastId().hashCode());
        result = prime * result + ((getQuantitativeIndicators() == null) ? 0 : getQuantitativeIndicators().hashCode());
        result = prime * result + ((getResults() == null) ? 0 : getResults().hashCode());
        result = prime * result + ((getRange() == null) ? 0 : getRange().hashCode());
        result = prime * result + ((getCreateBy() == null) ? 0 : getCreateBy().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", forecastId=").append(forecastId);
        sb.append(", quantitativeIndicators=").append(quantitativeIndicators);
        sb.append(", results=").append(results);
        sb.append(", range=").append(range);
        sb.append(", createBy=").append(createBy);
        sb.append(", createTime=").append(createTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}