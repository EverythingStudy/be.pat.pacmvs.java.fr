package cn.staitech.fr.controller;

import cn.hutool.json.JSONUtil;
import cn.staitech.common.core.domain.R;
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
@Api(value = "对外api", tags = {"V2.6.1"})
@Slf4j
@RestController
@RequestMapping("/api")
public class ApiController {

    @Resource
    private ParkDataProducer parkDataProducer;

    @ApiOperation(value = "py任务结果")
    @PostMapping("/pyResult")
    public R<String> pyResult(@RequestBody Map params) {
        String retData = JSONUtil.toJsonStr(params);
        log.info("Ai回调数据: " + retData);
        try {
            parkDataProducer.sendMessage(retData);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail(e.getMessage(), "失败");
        }
        return R.ok("", "成功");
    }

    @ApiOperation(value = "算法脏器识别任务")
    @PostMapping("/pyTask")
    public R<String> pyTask() {
        log.info("开始执行");
        parkDataProducer.sendDelayedMessage("test", 60 * 60 * 1000);
        log.info("结束执行");
        return R.ok("", "成功");
    }
}
