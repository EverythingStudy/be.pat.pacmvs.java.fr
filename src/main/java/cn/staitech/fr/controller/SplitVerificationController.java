package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.in.OrganDisassemblyQueryIn;
import cn.staitech.fr.domain.in.ResultCorrectionIn;
import cn.staitech.fr.domain.in.SplitVerificationQueryIn;
import cn.staitech.fr.domain.out.OrganDisassemblyOut;
import cn.staitech.fr.domain.out.SplitVerificationOut;
import cn.staitech.fr.service.OrganDisassemblyService;
import cn.staitech.fr.service.SplitVerificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 
* @ClassName: SplitVerificationController
* @Description:拆分核对
* @author wanglibei
* @date 2024年4月3日
* @version V1.0
 */
@Api(value = "专题管理-拆分核对", tags = "专题管理-拆分核对")
@RestController
@RequestMapping("/splitVerification")
public class SplitVerificationController {
    @Resource
    private SplitVerificationService splitVerificationService;

    @ApiOperation(value = "专题管理-拆分核对")
    @PostMapping("/list")
    public R<PageResponse<SplitVerificationOut>> list(@Validated @RequestBody SplitVerificationQueryIn req) {
        PageResponse<SplitVerificationOut> page = splitVerificationService.getList(req);
        return R.ok(page);
    }
    
    @ApiOperation(value = "专题管理-结果修正/取消修正")
    @PostMapping("/updateResult")
    public R updateResult(@Validated @RequestBody ResultCorrectionIn req) {
        splitVerificationService.updateResult(req);
        return R.ok();
    }

}
