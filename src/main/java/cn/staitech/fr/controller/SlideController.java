package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.CustomPage;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.core.web.controller.BaseController;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.vo.project.ChoiceImagePageReq;
import cn.staitech.fr.vo.project.ProjectImageVo;
import cn.staitech.fr.vo.project.slide.SlideDelVo;
import cn.staitech.fr.vo.project.slide.SlidePageReq;
import cn.staitech.fr.vo.project.ImageVO;
import cn.staitech.fr.vo.project.slide.SlidePageVo;
import cn.staitech.fr.vo.project.slide.SlideDetailVo;
import cn.staitech.fr.service.SlideService;
import cn.staitech.fr.utils.MessageSource;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * @author mugw
 * @version 2.6.0
 * @description 项目切片管理
 * @date 2025/5/14 13:44:14
 */
@Slf4j
@Api(value = "项目切片管理", tags = {"V2.6.0"})
@RestController
@RequestMapping("/slide")
public class SlideController  extends BaseController {

    @Resource
    private SlideService slideService;

    @ApiOperation(value = "项目阅片-已选切片分页查询")
    @PostMapping("/page")
    public R<CustomPage<SlidePageVo>> page(@Validated @RequestBody SlidePageReq req) {
        return slideService.page(req,false,true);
    }

    @ApiOperation(value = "项目阅片-已选切片无权限分页")
    @PostMapping("/pageNoAccessPermission")
    public R<CustomPage<SlidePageVo>> pageNoAccessPermission(@Validated @RequestBody SlidePageReq req) {
        return slideService.page(req,true,false);
    }

    @ApiOperation(value = "项目配置-已选切片分页查询")
    @PostMapping("/pageConfigSlide")
    public R<CustomPage<SlidePageVo>> pageConfigSlide(@Validated @RequestBody SlidePageReq req) {
        return slideService.page(req,true,true);
    }

    @ApiOperation(value = "项目配置-未选片分页查询")
    @PostMapping("/choiceImageList")
    public R<CustomPage<ImageVO>> choiceList(@Validated @RequestBody ChoiceImagePageReq image) {
        return slideService.choiceImageList(image);
    }

    @ApiOperation(value = "项目配置-选片保存")
    @PostMapping(value = "/choiceSave")
    public R choiceSave(@RequestBody @Validated ProjectImageVo choiceSaveInVo) {
        return slideService.choiceSave(choiceSaveInVo);
    }

    @ApiOperation(value = "项目配置-选片当前专题下原始切片保存")
    @PostMapping(value = "/choiceAll")
    public R choiceAll(@RequestParam(value = "projectId") @ApiParam(name = "projectId", value = "项目id") Long projectId) throws Exception {
        return slideService.choiceAll(projectId);
    }

    @ApiOperation(value = "项目配置-删除切片")
    @PostMapping("/deleteSlide")
    public R deleteSlide(@RequestParam(value = "projectId") @ApiParam(name = "projectId", value = "项目id") Long projectId) {
        return slideService.deleteSlide(projectId,null);
    }
    @ApiOperation(value = "项目配置-删除切片")
    @PostMapping("/deleteSlideByIds")
    public R deleteSlide(@RequestBody @Validated SlideDelVo slideDelIn) {
        return slideService.deleteSlide(slideDelIn.getProjectId(),slideDelIn.getSlideIds());
    }

    @ApiOperation(value = "更新单切片描述")
    @PostMapping("/updateDescription")
    public R getDataList(@RequestBody Slide slide) {
        boolean res = slideService.updateById(slide);
        if(res){
            return R.ok(MessageSource.M("OPERATE_SUCCEED"));
        }else {
            return R.fail(MessageSource.M("OPERATE_ERROR"));
        }
    }

    @ApiOperation(value = "矩阵阅片-切片维度(相邻切片)")
    @PostMapping("/slideAdjacent")
    public R<HashMap<String, SlidePageVo>> slideAdjacent(@RequestBody @Validated SlidePageReq req) {
        HashMap<String, SlidePageVo> resp = slideService.slideAdjacent(req);
        return R.ok(resp);
    }

    @ApiOperation(value = "查询切片、图片详情接口。调用后更新阅片状态")
    @GetMapping("/slideInfo")
    public R<SlideDetailVo> slideInfo(@RequestParam(value = "slideId") @ApiParam(name = "slideId", value = "标注id", required = true) Long slideId) {
        Slide slide = slideService.getById(slideId);
        return R.ok(slideService.getSlideInfo(slideId));
    }
    

}
