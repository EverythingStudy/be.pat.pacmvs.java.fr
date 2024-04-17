package cn.staitech.fr.domain.out;

import cn.staitech.fr.converter.DateExcelConverter;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author wmy
 * @version 1.0
 * @date 2024/4/2 17:57
 * @description
 */
@Data
public class ImageExportOut {
    @ApiModelProperty(value = "图像id", hidden = true)
    @ExcelIgnore
    private Long imageId;
    @ExcelProperty(value = "专题号")
    @ExcelIgnore
    private String topicName;
    @ExcelProperty(value = "切图编号")
    private String imageName;
    @ExcelProperty(value = "图像大小")
    private String size;
    @ExcelProperty(value = "切图编号")
    @ExcelIgnore
    private String imageCode;
    @ExcelProperty(value = "状态")
    @ExcelIgnore
    private Integer status;
    @ExcelProperty(value = "状态")
    private String fileStatus;
    @ExcelProperty(value = "上传时间", converter = DateExcelConverter.class)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @ExcelProperty(value = "机构")
    private String organizationName;
}
