package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.service.SingleSlideService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@Api(value = "ai预测结果", tags = "ai预测结果")
@Slf4j
@RestController
@RequestMapping("/aiForecast")
public class AiForecastController {

    @Resource
    private SingleSlideService singleSlideService;

    @ApiOperation(value = "预测结果", tags = {"V2.6.1"})
    @GetMapping("/forecastResults")
    public R<Boolean> forecastResults(@RequestParam(value = "singleSlideId") @ApiParam(name = "singleSlideId", value = "单切片ID", required = true) Long singleSlideId, @RequestParam(value = "imageId") @ApiParam(name = "imageId", value = "图片ID", required = true) Long imageId) {
        return R.ok(singleSlideService.forecastResults(singleSlideId, imageId));
    }
}
