package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.in.OrganDisassemblyQueryIn;
import cn.staitech.fr.domain.out.OrganDisassemblyOut;
import cn.staitech.fr.service.OrganDisassemblyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author wmy
 * @version 1.0
 * @date 2024/4/2 9:35
 * @description 脏器拆分
 */
@Api(value = "专题管理-脏器拆分", tags = "专题管理-脏器拆分")
@RestController
@RequestMapping("/organDisassembly")
public class OrganDisassemblyController {
    @Resource
    private OrganDisassemblyService organDisassemblyService;

    @ApiOperation(value = "专题管理-脏器拆分")
    @PostMapping("/list")
    public R<PageResponse<OrganDisassemblyOut>> list(@Validated @RequestBody OrganDisassemblyQueryIn req) {
        PageResponse<OrganDisassemblyOut> page = organDisassemblyService.getList(req);
        return R.ok(page);
    }

    @ApiOperation(value = "原始切片-导出按钮")
    @GetMapping("/export")
    public void export(@RequestParam(value = "imageIds") List<Long> imageIds) throws Exception {
        organDisassemblyService.export(imageIds);
    }


}
