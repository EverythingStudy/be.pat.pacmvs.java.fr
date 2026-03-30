package cn.staitech.fr.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AiForecastVo {

    private Long forecastId;
    private String results;
    private String fileUrl;
    private String isDelete = "0"; //0 表示未删除，1表示已删除
    private BigDecimal meanAddStandardDeviation;
    private BigDecimal meanSubStandardDeviation;
}
