package cn.staitech.fr.controller;


import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.in.StartPredictionIn;
import cn.staitech.fr.service.AlgorithmPredictionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 眼科切片预测表 前端控制器
 * </p>
 *
 * @author wanglibei
 * @since 2023-11-02
 */

@Api(value = "算法启动", tags = "算法启动")
@Slf4j
@RestController
@RequestMapping("/slideRecognition")
public class SlideRecognitionnController {

    @Resource
    private AlgorithmPredictionService algorithmPredictionService;

    @SuppressWarnings("rawtypes")
    @ApiOperation(value = "启动识别算法")
//    @RequiresPermissions(value = {"algorithmDetectionInfo:slice:startAlgorithm", "algorithmDetectionInfo:slice:reStartErrorData"}, logical = Logical.OR)
    @PostMapping("/start")
    public R start(@Validated @RequestBody StartPredictionIn req) {
        R r = algorithmPredictionService.startPrediction(req);
    	return r;
    }
    
}