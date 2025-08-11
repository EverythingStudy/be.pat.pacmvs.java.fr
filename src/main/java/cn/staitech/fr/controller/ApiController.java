package cn.staitech.fr.controller;

import cn.hutool.json.JSONUtil;
import cn.staitech.fr.config.ParkDataProducer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author mugw
 * @version 1.0
 * @description 外部调用api
 * @date 2024/5/10 10:37:32
 */
@Api(value = "对外api", tags = "对外api")
@Slf4j
@RestController
@RequestMapping("/api")
public class ApiController {

    @Resource
    private ParkDataProducer parkDataProducer;

    @ApiOperation(value = "py任务结果")
    @PostMapping("/pyResult")
    public void pyResult(@RequestBody Map params) {
    	String retData = JSONUtil.toJsonStr(params);
    	log.info("Ai回调数据: " + retData);
        parkDataProducer.sendMessage(retData);
    }
}
