package cn.staitech.fr.controller;

import cn.hutool.json.JSONUtil;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.config.OrganStructureConfig;
import cn.staitech.fr.config.OrganStructureConfig.OrganStructure;
import cn.staitech.fr.config.SpecialStructureConfig;
import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.AlgorithmCallBackService;
import cn.staitech.fr.service.JsonFileService;
import cn.staitech.fr.service.JsonTaskService;
import cn.staitech.fr.service.SingleSlideService;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.service.strategy.json.JsonTaskParserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

/**
 * @author wanglibei
 * @version V1.0
 * @ClassName: AlgorithmCallBackController
 * @Description:AI算法回调
 * @date 2025年8月11日
 */

@Api(value = "算法回调", tags = "算法回调")
@Slf4j
@RestController
@RequestMapping("/algorithmCallBack")
@Deprecated
public class AlgorithmCallBackController {

    @Resource
    private AiForecastService aiForecastService;

    @Resource
    private AlgorithmCallBackService algorithmCallBackService;
    
    @Resource
    private CommonJsonParser commonJsonParser;
    
    @Resource
    private SingleSlideService singleSlideService;
    
    @Resource
    JsonTaskService jsonTaskService;
    @Resource
    JsonFileService jsonFileService;
    
    @Resource
    JsonTaskParserService jsonTaskParserService;

    @Resource
    private OrganStructureConfig organStructureConfig;
    
    @Resource
    private SpecialStructureConfig specialStructureConfig;


    @SuppressWarnings("rawtypes")
    @ApiOperation(value = "结构回调")
    @PostMapping("/structure")
    public R structure(@RequestBody String data) {
        log.info("结构回调完整数据是：{}", data);
        algorithmCallBackService.input(data);
        return R.ok();
    }

    @ApiOperation(value = "预测结果")
    @GetMapping("/forecastResults")
    public R<Boolean> forecastResults(@RequestParam(value = "singleSlideId") @ApiParam(name = "singleSlideId", value = "单切片ID", required = true) Long singleSlideId, @RequestParam(value = "imageId") @ApiParam(name = "imageId", value = "图片ID", required = true) Long imageId) {
        return R.ok(aiForecastService.forecastResults(singleSlideId, imageId));
    }
    
    @ApiOperation(value = "test",hidden = true)
    @GetMapping("/test")
    public R<Boolean> test(@RequestParam(value = "sId") @ApiParam(name = "sId", value = "sId", required = true) String sId) {
//    	JsonTask jsonTask = jsonTaskService.getById(1065l);
//    	JsonFile jsonFile = jsonFileService.getById(6677l);
//    	commonJsonParser.parseTissueContourJson(jsonTask, jsonFile);
        //计算面积和周长，并更新到fr_single_slide表里
//    	singleSlideService.forecastResults(jsonTask.getSingleId(), jsonTask.getImageId());
    	
//    	"01": # 肾上腺
//        - structure-id: 10103D,10103E,101068,101004,
//        
//      31B: # 唾液腺，腮腺
//        - structure-id: 31B03E,31B03D,31B026,31B031,31B030,31B02E,31B02F,31B111
//        
    	
//        List<OrganStructureConfig.OrganStructure> structures = organStructureConfig.getStructures().get("101");
//        if(CollectionUtils.isNotEmpty(structures)) {
//            List<String> enabledStructures = Arrays.asList(structures.get(0).getStructureId().split(","));
//            System.out.println("ennnn1============="+enabledStructures.toString());
//        }
//        
//        List<OrganStructureConfig.OrganStructure> structures2 = organStructureConfig.getStructures().get("31B");
//        if(CollectionUtils.isNotEmpty(structures2)) {
//            List<String> enabledStructures = Arrays.asList(structures2.get(0).getStructureId().split(","));
//            System.out.println("ennnn2============="+enabledStructures.toString());
//        }
        
    	List<String> sList = specialStructureConfig.getStructureIds();
    	System.out.println("特殊的结构编码列表==>"+sList.toString());
    	List<OrganStructureConfig.OrganStructure> structures2 = organStructureConfig.getStructures().get(sId);
      if(CollectionUtils.isNotEmpty(structures2)) {
          List<String> enabledStructures = Arrays.asList(structures2.get(0).getStructureId().split(","));
          System.out.println(sId+" 对应的结构编码如下============="+enabledStructures.toString());
      }
        return R.ok(true);
    }

}