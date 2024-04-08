package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.in.ChoiceImageListInVo;
import cn.staitech.fr.domain.in.ChoiceSaveInVo;
import cn.staitech.fr.domain.in.SlideListQueryIn;
import cn.staitech.fr.domain.out.ImageListOutVO;
import cn.staitech.fr.domain.out.SlideListQueryOut;
import cn.staitech.fr.domain.out.SlideSelectBy;
import cn.staitech.fr.service.ImageService;
import cn.staitech.fr.service.SlideService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author wudi
 * @Date 2024/4/1 9:22
 * @desc
 */
@Slf4j
@Api(value = "专题切片", tags = "专题切片")
@RestController
@RequestMapping("/slide")
public class SlideController {

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


    @ApiOperation(value = "查询切片、图片详情接口")
    @GetMapping("/slideInfo")
    public R<SlideSelectBy> slideInfo(@RequestParam(value = "slideId") @ApiParam(name = "slideId", value = "标注id", required = true) Long slideId) {
        return R.ok(slideService.pageImageCsvListVOBy(slideId));
    }
}
