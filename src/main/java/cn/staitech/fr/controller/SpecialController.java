package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.log.annotation.Log;
import cn.staitech.common.log.enums.BusinessType;
import cn.staitech.common.security.annotation.Logical;
import cn.staitech.common.security.annotation.RequiresPermissions;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.domain.Special;
import cn.staitech.fr.domain.in.EditSpecialStatusIn;
import cn.staitech.fr.domain.in.SpecialAddIn;
import cn.staitech.fr.domain.in.SpecialEditIn;
import cn.staitech.fr.domain.in.SpecialListQueryIn;
import cn.staitech.fr.domain.in.WaxBlockNumberListIn;
import cn.staitech.fr.domain.out.SpecialListQueryOut;
import cn.staitech.fr.domain.out.WaxBlockNumberListOut;
import cn.staitech.fr.service.SpecialService;
import cn.staitech.system.api.domain.SysUser;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author wudi
 * @Date 2024/3/29 14:17
 * @desc
 */
@Api(value = "专题", tags = "专题")
@RestController
@RequestMapping("/special")
public class SpecialController {

    @Autowired
    private SpecialService specialService;

    @ApiOperation(value = "专题列表分页查询")
    @PostMapping("/list")
    public R<PageResponse<SpecialListQueryOut>> list(@RequestBody @Validated SpecialListQueryIn req) {
        PageResponse<SpecialListQueryOut> resp = specialService.getSpecialList(req);
        return R.ok(resp);
    }

    @ApiOperation(value = "专题新增")
    @PostMapping("/add")
    public R add(@RequestBody @Validated SpecialAddIn req) {
        return specialService.addSpecial(req);

    }

    @ApiOperation(value = "专题新增")
    @GetMapping("/info")
    public R<Special> info(@RequestParam("specialId") @ApiParam(name = "specialId", value ="专题id" ) Long specialId){
        return R.ok(specialService.getById(specialId));

    }

    @ApiOperation(value = "专题修改")
    @PostMapping("/edit")
    public R edit(@RequestBody @Validated SpecialEditIn req) {
        return specialService.editSpecial(req);
    }

    @ApiOperation(value = "专题删除")
    @GetMapping("/remove")
    public R remove(@RequestParam("specialId") @ApiParam(name = "specialId", value ="专题id" ) Long specialId) {
        return specialService.removeSpecial(specialId);
    }

    @ApiOperation(value = "编辑专题状态")
    @PostMapping("/editStatus")
    public R editStatus(@Validated @RequestBody EditSpecialStatusIn req) {
        return specialService.editSpecialStatus(req);

    }

}
