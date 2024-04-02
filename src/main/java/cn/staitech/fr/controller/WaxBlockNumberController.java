package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.WaxBlockInfo;
import cn.staitech.fr.domain.in.UploadWaxBlockIn;
import cn.staitech.fr.domain.in.WaxBlockNumberEditIn;
import cn.staitech.fr.domain.in.WaxBlockNumberListIn;
import cn.staitech.fr.domain.out.WaxBlockNumberListOut;
import cn.staitech.fr.service.WaxBlockInfoService;
import cn.staitech.fr.service.WaxBlockNumberService;
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
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
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
@Validated
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



    @ApiOperation(value = "蜡块编号表修改")
    @PutMapping("/edit")
    public R edit(@RequestBody @Validated WaxBlockNumberEditIn req) {

        return waxBlockNumberService.edit(req);
    }

    @ApiOperation(value = "蜡块编号表删除")
    @DeleteMapping("{/numberId}")
    public R remove(@PathVariable("numberId") Long numberId) {

        return waxBlockNumberService.delete(numberId);
    }


    @ApiOperation(value = "蜡块编号表上传")
    @GetMapping("/upload")
    public R upload(@RequestParam("file") MultipartFile file,
                    @RequestParam(value = "organizationId", required = false) @NotNull(message = "机构id不能为空") @ApiParam(name = "organizationId", value = "机构id", required = true) Long organizationId,
                    @RequestParam(value = "topicId", required = false) @NotNull(message = "专题id不能为空") @ApiParam(name = "topicId", value = "专题id", required = true) Long topicId,
                    @RequestParam(value = "topicName", required = false) @ApiParam(name = "topicName", value = "专题名称") String topicName,
                    @RequestParam(value = "speciesId", required = false)  @ApiParam(name = "speciesId", value = "种属id", required = true) String speciesId,
                    @RequestParam(value = "speciesName", required = false)  @ApiParam(name = "speciesName", value = "种属名称", required = true) String speciesName
                    ) throws IOException {
        UploadWaxBlockIn req = new UploadWaxBlockIn();
        req.setFile(file);
        req.setOrganizationId(organizationId);
        req.setTopicId(topicId);
        req.setTopicName(topicName);
        req.setSpeciesId(speciesId);
        req.setSpeciesName(speciesName);
        return waxBlockNumberService.upload(req);
    }

    @ApiOperation(value = "蜡块编号信息预览列表")
    @GetMapping("/waxPreview")

    public R<List<WaxBlockInfo>> waxPreview( @RequestParam(value = "numberId",required = false)
                                                 @NotNull(message = "不能为空")
                                                 @ApiParam(name="numberId",value = "蜡块编号信息id",required = true) Long numberId)  {
        return waxBlockInfoService.waxPreview(numberId);
    }















}
