package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.CustomPage;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.core.web.controller.BaseController;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.mapper.SlideMapper;
import cn.staitech.fr.vo.project.*;
import cn.staitech.fr.vo.project.slide.*;
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
import java.util.List;

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
    @Resource
    private SlideMapper slideMapper;

    @ApiOperation(value = "获取动物编号下拉列表")
    @PostMapping("/getAnimalCode")
    public R<SlideSelectListVo> getAnimalCode(@RequestBody SlideSelectListReq req) {
        return R.ok(slideService.getAnimalCode(req));
    }

    @ApiOperation(value = "获取蜡块下拉列表")
    @PostMapping("/getWaxCode")
    public R<SlideSelectListVo> getWaxCode(@RequestBody SlideSelectListReq req) {
        return R.ok(slideService.getWaxCode(req));
    }

    @ApiOperation(value = "获取组号下拉列表")
    @PostMapping("/getGroupCode")
    public R<SlideSelectListVo> getGroupCode(@RequestBody SlideSelectListReq req) {
        return R.ok(slideService.getGroupCode(req));
    }

    //@TODO 切片脏器下拉列表
    @ApiOperation(value = "获取脏器下拉列表")
    @PostMapping("/getOrganCode")
    public R<SlideSelectListVo> getOrganCode(@RequestBody SlideSelectListReq req) {
        return R.ok(slideService.getOrganCode(req));
    }


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
    public R deleteSlide(@RequestParam(value = "projectId") @ApiParam(name = "projectId", value = "项目id") Long projectId)throws  Exception {
        return slideService.deleteSlide(projectId,null);
    }

    @ApiOperation(value = "项目配置-删除切片")
    @PostMapping("/deleteSlideByIds")
    public R deleteSlideByIds(@RequestBody @Validated SlideDelVo slideDelIn)throws  Exception {
        return slideService.deleteSlide(slideDelIn.getProjectId(),slideDelIn.getSlideIds());
    }

    @ApiOperation(value = "项目配置-检查删除切片")
    @PostMapping("/checkDeleteSlide")
    public R checkDeleteSlide(@RequestBody @Validated SlideDelVo slideDelIn)throws  Exception {
        return slideService.checkDeleteSlide(slideDelIn.getProjectId(),slideDelIn.getSlideIds());
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
        SlideDetailVo slideInfo = slideMapper.getSlideInfo(slideId);
        return R.ok(slideInfo);
    }

    @ApiOperation(value = "阅片记录")
    @GetMapping("/visit")
    public R visit(@RequestParam(value = "slideId") @ApiParam(name = "slideId", value = "标注id", required = true) Long slideId) {
        return R.ok(slideService.getSlideInfo(slideId));
    }

    @ApiOperation(value = "AI分析")
    @PostMapping("/aiAnalysis")
    public R<String> aiAnalysis(@RequestBody @Validated AiAnalysisReq req) {
        return this.slideService.aiAnalysis(req);
    }

    @ApiOperation(value = "脏器识别校对-python服务使用")
    @PostMapping("/organCheck")
    public R<OrganCheckVo> organCheck(@RequestBody @Validated OrganCheckReq req) {
        return R.ok(this.slideService.organCheck(req));
    }

    @ApiOperation(value = "脏器识别校对-view页面数据")
    @PostMapping("/organCheckView")
    public R<OrganCheckViewVo> organCheckView(@RequestBody @Validated OrganCheckViewReq req) {
        return R.ok(this.slideService.organCheckView(req));
    }

    @ApiOperation(value = "脏器识别校对-确认修改")
    @PostMapping("/organCheckConfirm")
    public R<String> organCheckConfirm(@RequestBody @Validated OrganCheckViewReq req) {
        this.slideService.organCheckConfirm(req);
        return R.ok();
    }
}
