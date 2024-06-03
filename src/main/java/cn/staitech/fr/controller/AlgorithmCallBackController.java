package cn.staitech.fr.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import cn.hutool.json.JSONUtil;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.domain.in.AlgorithmAnnIn;
import cn.staitech.fr.mapper.SlideMapper;
import cn.staitech.fr.service.AlgorithmPredictionService;
import cn.staitech.fr.service.ImageService;
import cn.staitech.fr.service.strategy.json.JsonTaskParserService;
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
    @Resource
    private JsonTaskParserService jsonTaskParserService;
    
    @Resource
	private SlideMapper slideMapper;
    
    @SuppressWarnings("rawtypes")
	@ApiOperation(value = "脏器识别回调")
	@PostMapping("/recognition")
	public R recognition(@Validated @RequestBody AlgorithmAnnIn  algorithmAnnIn) {
		log.info("脏器识别算法回调专题,完整数据是：{}",JSONUtil.toJsonStr(algorithmAnnIn));
		// 多线程处理
        algorithmPredictionService.recognition(algorithmAnnIn);
		return R.ok();
	}
    
    
    @SuppressWarnings("rawtypes")
	@ApiOperation(value = "脏器识别回调(按专题)")
	@PostMapping("/recognitionBySpecial")
	public R recognitionBySpecial(@Validated @RequestBody AlgorithmAnnIn  algorithmAnnIn) {
		log.info("脏器识别算法回调专题,完整数据是：{}",JSONUtil.toJsonStr(algorithmAnnIn));
		// 多线程处理
		//专题id 185 全部从新核对
		QueryWrapper<Slide> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("special_id",algorithmAnnIn.getSlideId());
		queryWrapper.eq("check_status",0);
		//逻辑删除状态（0存在，1删除）
		queryWrapper.eq("del_flag",CommonConstant.NUMBER_0);
		List<Slide> slideList = slideMapper.selectList(queryWrapper);
		if(CollectionUtils.isNotEmpty(slideList)){
			List<Long> slideIdList = slideList.stream().map(Slide::getSlideId).collect(Collectors.toList());
			for(Long slideId:slideIdList){
				AlgorithmAnnIn  anno = new AlgorithmAnnIn();
				anno.setSlideId(slideId);
				anno.setOrganizationId(6L);
				algorithmPredictionService.recognition(anno);
				try {
					Thread.sleep(10L);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return R.ok();
	}
    
    
    @SuppressWarnings("rawtypes")
	@ApiOperation(value = "AI预测回调")
	@PostMapping("/aiTest")
    public R test(@Validated @RequestBody String  str) {
    	log.info("脏器识别算法回调专题,完整数据是：{}",str);
    	jsonTaskParserService.input(str);
    	return R.ok();
    }
    
}