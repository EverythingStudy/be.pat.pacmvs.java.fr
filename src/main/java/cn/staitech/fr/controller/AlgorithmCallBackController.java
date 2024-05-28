package cn.staitech.fr.controller;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.json.JSONUtil;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.in.AlgorithmAnnIn;
import cn.staitech.fr.service.AlgorithmPredictionService;
import cn.staitech.fr.service.ImageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * 
* @ClassName: DefinitionCallBackController
* @Description:算法清晰度回调
* @author wanglibei
* @date 2024年3月29日
* @version V1.0
 */
@Api(value = "社区版算法回调", tags = "社区版算法回调")
@Slf4j
@RestController
@RequestMapping("/algorithmCallBack")
public class AlgorithmCallBackController {

    @Resource
    private ImageService imageService;
    
    @Resource
    private AlgorithmPredictionService algorithmPredictionService;

    
    @SuppressWarnings("rawtypes")
	@ApiOperation(value = "脏器识别回调")
	@PostMapping("/recognition")
	public R recognition(@Validated @RequestBody AlgorithmAnnIn  algorithmAnnIn) {
		log.info("脏器识别算法回调专题,完整数据是：{}",JSONUtil.toJsonStr(algorithmAnnIn));
		// 多线程处理
        algorithmPredictionService.recognition(algorithmAnnIn);
		return R.ok();
	}
    
}