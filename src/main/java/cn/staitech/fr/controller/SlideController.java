package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.core.web.controller.BaseController;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.domain.in.ChoiceImageListInVo;
import cn.staitech.fr.domain.in.ChoiceSaveInVo;
import cn.staitech.fr.domain.in.SlideListQueryIn;
import cn.staitech.fr.domain.out.ImageListOutVO;
import cn.staitech.fr.domain.out.SlideListQueryOut;
import cn.staitech.fr.domain.out.SlideSelectBy;
import cn.staitech.fr.service.ImageService;
import cn.staitech.fr.service.SlideService;
import cn.staitech.fr.utils.MessageSource;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * @Author wudi
 * @Date 2024/4/1 9:22
 * @desc
 */
@Slf4j
@Api(value = "专题切片", tags = "专题切片")
@RestController
@RequestMapping("/slide")
public class SlideController  extends BaseController {

    @Autowired
    private ImageService imageService;

    @Autowired
    private SlideService slideService;


    @ApiOperation(value = "选片列表-选前")
    @PostMapping("/choiceImageList")
    public R<PageResponse<ImageListOutVO>> choiceList(@Validated @RequestBody ChoiceImageListInVo image) {
        PageResponse<ImageListOutVO> page = imageService.choiceImageList(image);
        return R.ok(page);
    }

    @ApiOperation(value = "选片保存")
    @PostMapping(value = "/choiceSave")
    public R choiceSave(@RequestBody @Validated ChoiceSaveInVo choiceSaveInVo) {
        return slideService.choiceSave(choiceSaveInVo);
    }

    @ApiOperation(value = "选片列表-选后")
    @PostMapping("/list")
    public R<PageResponse<SlideListQueryOut>> list(@Validated @RequestBody SlideListQueryIn req) {
        PageResponse<SlideListQueryOut> page = slideService.slideListQuery(req);
        return R.ok(page);
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
    public R<HashMap<String, SlideListQueryOut>> slideAdjacent(@RequestBody @Validated SlideListQueryIn req) {
        HashMap<String, SlideListQueryOut> resp = slideService.slideAdjacent(req);
        return R.ok(resp);
    }


    @ApiOperation(value = "查询切片、图片详情接口")
    @GetMapping("/slideInfo")
    public R<SlideSelectBy> slideInfo(@RequestParam(value = "slideId") @ApiParam(name = "slideId", value = "标注id", required = true) Long slideId) {
        return R.ok(slideService.pageImageCsvListVOBy(slideId));
    }

    @ApiOperation(value = "选片列表-选后-删除")
    @GetMapping("/delete")
    public R deleteById(@RequestParam(value = "slideId") @ApiParam(name = "slideId", value = "标注id", required = true) Long slideId) {
        return slideService.deleteById(slideId);

    }
    
    @ApiOperation(value = "选片列表-全部删除")
    @GetMapping("/deleteAll")
    public R deleteAll(@RequestParam(value = "specialId",required = true) @ApiParam(name = "specialId", value = "专题id") Long specialId,
                       @RequestParam(value = "slideId",required = false) @ApiParam(name = "slideId", value = "切片id") Long slideId) {
        return slideService.deleteAll(specialId,slideId);

    }
}
