package cn.staitech.fr.converter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author wmy
 * @version 1.0
 * @date 2024/4/3 10:46
 * @description
 */
public class DateExcelConverter implements Converter<Date> {
    private static final String PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Override
    public Class<?> supportJavaTypeKey() {
        return Converter.super.supportJavaTypeKey();
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return Converter.super.supportExcelTypeKey();
    }

    @Override
    public WriteCellData<?> convertToExcelData(Date value, ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN);
        String dateValue = sdf.format(value);
        return new WriteCellData<>(dateValue);
    }

}
