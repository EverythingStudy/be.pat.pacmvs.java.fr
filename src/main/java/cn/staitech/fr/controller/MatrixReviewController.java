package cn.staitech.fr.controller;

import cn.staitech.fr.domain.in.AiDownloadIn;
import cn.staitech.fr.service.MatrixReviewService;
import cn.staitech.fr.vo.project.slide.SlidePageReq;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author wudi
 * @Date 2024/4/10 15:50
 * @desc
 */
@Api(value = "专题阅片-矩阵阅片", tags = "V2.6.1")
@RestController
@RequestMapping("/matrixReview")
public class MatrixReviewController {

    @Autowired
    private MatrixReviewService matrixReviewService;


//    @ApiOperation(value = "算法报告下载")
//    @PostMapping("/algorithmDownload")
//    public void algorithmDownload(@Validated @RequestBody AiDownloadIn req) throws Exception {
//        matrixReviewService.algorithmDownload(req);
//    }
    @ApiOperation(value = "算法报告下载")
    @PostMapping("/algorithmDownload")
    public void algorithmDownload(@Validated @RequestBody SlidePageReq req) throws Exception {
        matrixReviewService.algorithmDownload(req);
    }

}
