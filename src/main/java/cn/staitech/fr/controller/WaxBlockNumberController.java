package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.log.annotation.Log;
import cn.staitech.common.log.enums.BusinessType;
import cn.staitech.common.security.annotation.Logical;
import cn.staitech.common.security.annotation.RequiresPermissions;
import cn.staitech.fr.domain.WaxBlockInfo;
import cn.staitech.fr.domain.in.UploadWaxBlockIn;
import cn.staitech.fr.domain.in.WaxBlockNumberEditIn;
import cn.staitech.fr.domain.in.WaxBlockNumberListIn;
import cn.staitech.fr.domain.out.WaxBlockNumberListOut;
import cn.staitech.fr.service.WaxBlockInfoService;
import cn.staitech.fr.service.WaxBlockNumberService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @Author wudi
 * @Date 2024/3/28 16:16
 * @desc
 */
@Api(value = "蜡块编号表", tags = "蜡块编号表")
@RestController
@RequestMapping("/wax")
public class WaxBlockNumberController {
    @Autowired
    private WaxBlockNumberService waxBlockNumberService;

    @Autowired
    private WaxBlockInfoService waxBlockInfoService;
    @ApiOperation(value = "蜡块编号表列表分页查询")
    @PostMapping("/list")
    public R<PageResponse<WaxBlockNumberListOut>> list(@RequestBody @Validated WaxBlockNumberListIn req) {
        PageResponse<WaxBlockNumberListOut> resp = waxBlockNumberService.getWaxList(req);
        return R.ok(resp);
    }



    @ApiOperation(value = "蜡块编号表列表分页查询")
    @PutMapping("/edit")
    public R edit(@RequestBody @Validated WaxBlockNumberEditIn req) {

        return waxBlockNumberService.edit(req);
    }

    @ApiOperation(value = "蜡块编号表列表分页查询")
    @DeleteMapping("{/id}")
    public R remove(@PathVariable("id") Long id) {

        return waxBlockNumberService.delete(id);
    }


    @ApiOperation(value = "蜡块编号表上传")
    @PostMapping("/upload")
    public R upload(@RequestBody @Validated UploadWaxBlockIn req) throws IOException {
        return waxBlockNumberService.upload(req);
    }

    @ApiOperation(value = "蜡块编号信息预览列表")
    @GetMapping("/waxPreview")
    public R<List<WaxBlockInfo>> waxPreview(@RequestParam("id") @ApiParam(name="id",value = "蜡块编号信息id") Long id) throws IOException {
        return waxBlockInfoService.waxPreview(id);
    }















}
