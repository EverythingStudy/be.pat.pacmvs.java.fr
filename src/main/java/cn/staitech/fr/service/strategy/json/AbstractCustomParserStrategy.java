package cn.staitech.fr.service.strategy.json;

import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


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
}
