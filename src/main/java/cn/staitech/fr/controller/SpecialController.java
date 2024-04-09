package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.core.web.controller.BaseController;
import cn.staitech.fr.constant.Container;
import cn.staitech.fr.domain.Special;
import cn.staitech.fr.domain.in.EditSpecialStatusIn;
import cn.staitech.fr.domain.in.SpecialAddIn;
import cn.staitech.fr.domain.in.SpecialEditIn;
import cn.staitech.fr.domain.in.SpecialListQueryIn;
import cn.staitech.fr.domain.out.SpecialListQueryOut;
import cn.staitech.fr.service.SpecialService;
import cn.staitech.fr.utils.LanguageUtils;
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

import java.util.Map;

/**
 * @Author wudi
 * @Date 2024/3/29 14:17
 * @desc
 */
@Api(value = "专题", tags = "专题")
@RestController
@RequestMapping("/special")
public class SpecialController  extends BaseController {

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

    @ApiOperation(value = "专题详情")
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

    @ApiOperation(value = "染色类型列表", notes = "染色类型列表")
    @GetMapping("/colorType")
    public R<Map<Integer, String>> colorType() {
        Map<Integer, String> map;
        if (LanguageUtils.isEn()) {
            map = Container.COLOR_TYPE_EN;
        } else {
            map = Container.COLOR_TYPE;
        }
        return R.ok(map);
    }

    @ApiOperation(value = "试验类型列表", notes = "试验类型列表")
    @GetMapping("/trialType")
    public R<Map<Integer, String>> trialType() {
        Map<Integer, String> map;
        if (LanguageUtils.isEn()) {
            map = Container.TRIAL_TYPE_EN;
        } else {
            map = Container.TRIAL_TYPE;
        }
        return R.ok(map);
    }

    @ApiOperation(value = "专题状态列表", notes = "专题状态列表")
    @GetMapping("/specialStatus")
    public R<Map<Integer, String>> specialStatus() {
        Map<Integer, String> map;
        if (LanguageUtils.isEn()) {
            map = Container.SPECIAL_STATUS_EN;
        } else {
            map = Container.SPECIAL_STATUS;
        }
        return R.ok(map);
    }

}
