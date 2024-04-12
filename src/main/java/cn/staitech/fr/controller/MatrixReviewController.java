package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.PageDataResponse;
import cn.staitech.fr.domain.in.MatrixReviewEditIn;
import cn.staitech.fr.domain.in.MatrixReviewListIn;
import cn.staitech.fr.domain.out.AnimalDimensionOut;
import cn.staitech.fr.domain.out.MatrixReviewListOut;
import cn.staitech.fr.domain.out.MatrixReviewOut;
import cn.staitech.fr.service.MatrixReviewService;
import cn.staitech.fr.service.SpecialService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author wudi
 * @Date 2024/4/10 15:50
 * @desc
 */
@Api(value = "专题阅片-矩阵阅片", tags = "专题阅片-矩阵阅片")
@RestController
@RequestMapping("/matrixReview")
public class MatrixReviewController {

    @Autowired
    private SpecialService specialService;
    @Autowired
    private MatrixReviewService matrixReviewService;


    @ApiOperation(value = "矩阵阅片-对照组数据列表")
    @GetMapping("/groupList")
    public R<List<MatrixReviewOut>> groupList(@RequestParam(value = "specialId") @ApiParam(name = "specialId", value = "专题id", required = true) Long specialId) {
        return matrixReviewService.groupList(specialId);
    }

    @ApiOperation(value = "矩阵阅片-设置对照组")
    @PostMapping("/edit")
    public R edit(@Validated @RequestBody MatrixReviewEditIn req) {
        return matrixReviewService.edit(req);
    }

    @ApiOperation(value = "矩阵阅片-切片维度")
    @PostMapping("/slideList")
    public R<PageResponse<MatrixReviewListOut>> list(@RequestBody @Validated MatrixReviewListIn req) {
        PageResponse<MatrixReviewListOut> resp = matrixReviewService.getMatrixReview(req);
        return R.ok(resp);
    }

    @ApiOperation(value = "矩阵阅片-动物编号维度")
    @PostMapping("/animalList")
    public R<PageDataResponse<AnimalDimensionOut>> animalList(@RequestBody @Validated MatrixReviewListIn req) {
        PageDataResponse<AnimalDimensionOut> resp = matrixReviewService.animalList(req);
        return R.ok(resp);
    }
}
