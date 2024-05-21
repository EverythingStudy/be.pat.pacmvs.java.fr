package cn.staitech.fr.service.strategy.json;

import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;

/**
 * @author mugw
 * @version 1.0
 * @description
 * @date 2024/5/14 11:13:10
 */
@Slf4j
@Data
public abstract class AbstractCustomParserStrategy implements CustomParserStrategy{

    private CommonJsonParser commonJsonParser;

    @Override
    public void parseJson(JsonTask jsonTask, JsonFile jsonFileS) {
        commonJsonParser.parseJson(jsonTask, jsonFileS);
    }

    /**
     * 获取脏器轮廓面积
     *
     * @param jsonTask    jsonTask
     * @param structureId 结构ID
     * @return 脏器面积-平方毫米
     */
    protected Annotation getOrganArea(JsonTask jsonTask, String structureId) {
        return commonJsonParser.getOrganArea(jsonTask,structureId);
    }

    protected Annotation getOrganArea(JsonTask jsonTask, String structureId,BigDecimal unit) {
        Annotation annotation = commonJsonParser.getOrganArea(jsonTask,structureId);
        BigDecimal bigDecimal = annotation.getStructureAreaNum();
        if (bigDecimal!=null){
            annotation.setStructureAreaNum(bigDecimal.multiply(unit));
        }
        return annotation;
    }

    /**
     * 取脏器轮廓数量
     *
     * @param jsonTask    jsonTask
     * @param structureId 结构ID
     * @return 脏器轮廓数量
     */
    protected Integer getOrganAreaCount(JsonTask jsonTask, String structureId) {
        return commonJsonParser.getOrganAreaCount(jsonTask,structureId);
    }

}
