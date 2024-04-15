package cn.staitech.fr.controller;


import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.out.SingleSlideSelectBy;
import cn.staitech.fr.service.SingleSlideService;
import cn.staitech.fr.utils.MessageSource;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@Api(value = "单脏器切片", tags = "单脏器切片")
@RestController
@Validated
@RestControllerAdvice
@RequestMapping("/singleSlide")
public class SingleSlideController {


    @Resource
    private SingleSlideService singleSlideService;

    @ApiOperation(value = "更新单切片描述")
    @PostMapping("/updateDescription")
    public R getDataList(
            @RequestParam(value = "singleId") @ApiParam(name = "singleId", value = "专题id", required = true) Long singleId,
            @RequestParam(value = "description") @ApiParam(name = "description", value = "描述", required = true) String description) {
        SingleSlide singleSlide = new SingleSlide();
        singleSlide.setSingleId(singleId);
        singleSlide.setDescription(description);
        boolean res = singleSlideService.updateById(singleSlide);
        if(res){
            return R.ok(MessageSource.M("OPERATE_SUCCEED"));
        }else {
            return R.fail(MessageSource.M("OPERATE_ERROR"));
        }
    }



    @ApiOperation(value = "查询切片、图片详情接口")
    @GetMapping("/singleSlideBy")
    public R<SingleSlideSelectBy> slideInfo(@RequestParam(value = "singleId") @ApiParam(name = "singleId", value = "单脏器切片id", required = true) Long singleId) {
        return R.ok(singleSlideService.singleSlideBy(singleId));
    }





}
