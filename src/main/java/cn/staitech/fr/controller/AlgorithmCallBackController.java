package cn.staitech.fr.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.AlgorithmCallBackService;
import cn.staitech.fr.service.strategy.json.JsonTaskParserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

/**
 * 
* @ClassName: AlgorithmCallBackController
* @Description:AI算法回调
* @author wanglibei
* @date 2025年8月11日
* @version V1.0
 */

@Api(value = "算法回调", tags = "算法回调")
@Slf4j
@RestController
@RequestMapping("/algorithmCallBack")
public class AlgorithmCallBackController {

    @Resource
    private AiForecastService aiForecastService;
    
    @Resource
    private AlgorithmCallBackService algorithmCallBackService;
    
    @Resource
    private JsonTaskParserService jsonTaskParserService;
    
    @SuppressWarnings("rawtypes")
	@ApiOperation(value = "结构回调")
	@PostMapping("/structure")
    public R structure(@RequestBody String  data) {
    	log.info("结构回调完整数据是：{}",data);
    	algorithmCallBackService.input(data);
    	return R.ok();
    }
    
    @ApiOperation(value = "预测结果")
    @GetMapping("/forecastResults")
    public R<Boolean> forecastResults(@RequestParam(value = "singleSlideId") @ApiParam(name = "singleSlideId", value = "单切片ID", required = true) Long singleSlideId, @RequestParam(value = "imageId") @ApiParam(name = "imageId", value = "图片ID", required = true) Long imageId) {
        return R.ok(aiForecastService.forecastResults(singleSlideId,imageId));
    }
    
}