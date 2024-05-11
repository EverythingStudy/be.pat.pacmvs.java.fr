package cn.staitech.fr.service.strategy.json;

import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.service.JsonTaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: wangfeng
 * @create: 2024-05-10 15:52:39
 * @Description:
 */
@Service
@Slf4j
public class JsonTaskParserService {

    @Resource
    JsonTaskService jsonTaskService;

    public void input(String input) {

//        JSONObject jsonObject = JSON.parseObject(input);
//
//        log.info("code:{}", jsonObject.get("code"));
//        log.info("fileurl_list:{}", jsonObject.get("fileurl_list"));
//
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonTask jsonTask = mapper.readValue(input, JsonTask.class);
            System.out.println(jsonTask.getCode());
            System.out.println(jsonTask.getJsonPath());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
