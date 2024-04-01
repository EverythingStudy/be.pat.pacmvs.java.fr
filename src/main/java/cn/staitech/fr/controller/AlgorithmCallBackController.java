package cn.staitech.fr.controller;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.json.JSONUtil;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.Image;
import cn.staitech.fr.domain.in.AlgorithmAnnIn;
import cn.staitech.fr.domain.in.DefinitionIn;
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

    @ApiOperation(value = "清晰度回调")
    @PostMapping("/verification")
    public R verification(@Validated @RequestBody DefinitionIn req) {
    	if (null != req) {
    		log.info("清晰度算法回调,result：{},完整数据是：{}",JSONUtil.toJsonStr(req));
    		Long imageId = req.getImageId();
    		//AI分析状态：0:待分析（初始状态）、1:AI分析中、2:AI分析成功、3:AI分析失败 4：部分分析成功
    		int aiAnalyzed = req.getAiAnalyzed();
    		//文件状态:0上传中、1上传失败、2解析中、3解析失败、4可用 5:不可用
    		int userStatus = 5;
    		/*if (aiAnalyzed == 2 || aiAnalyzed == 4) {
    			userStatus = 4;
    		}*/
    		userStatus = 4;
    		Image image = new Image();
    		image.setImageId(imageId);
    		image.setStatus(userStatus);
//    		imageService.updateById(image);
    	}
        return R.ok();
    }
    
    
    @SuppressWarnings("rawtypes")
	@ApiOperation(value = "脏器识别回调")
	@PostMapping("/recognition")
	public R recognition(@Validated @RequestBody AlgorithmAnnIn  algorithmAnnIn) throws Exception {
		log.info("脏器识别算法回调专题id:{},specialImageId：{},完整数据是：{}",algorithmAnnIn.getSpecialId(),algorithmAnnIn.getSpecialImageId(),JSONUtil.toJsonStr(algorithmAnnIn));
		//specialImageAnnoService.callBackAnnoResult(algorithmAnnIn);
		return R.ok();
	}



}