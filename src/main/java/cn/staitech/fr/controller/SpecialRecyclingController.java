package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.core.web.controller.BaseController;
import cn.staitech.fr.domain.in.SpecialListQueryIn;
import cn.staitech.fr.domain.in.SpecialRecyclingListQueryIn;
import cn.staitech.fr.domain.in.SpecialRecyclingRecoverIn;
import cn.staitech.fr.domain.out.SpecialListQueryOut;
import cn.staitech.fr.domain.out.SpecialRecyclingListQueryOut;
import cn.staitech.fr.service.SpecialRecyclingService;
import cn.staitech.fr.service.SpecialService;
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
 * @Date 2024/3/29 16:45
 * @desc
 */
@Api(value = "专题回收站", tags = "专题回收站")
@RestController
@RequestMapping("/specialRecycling")
public class SpecialRecyclingController  extends BaseController {

    @Autowired
    private SpecialRecyclingService specialRecyclingService;

    @ApiOperation(value = "专题回收站分页查询")
    @PostMapping("/list")
    public R<PageResponse<SpecialRecyclingListQueryOut>> list(@RequestBody @Validated SpecialRecyclingListQueryIn req) {
        PageResponse<SpecialRecyclingListQueryOut> resp = specialRecyclingService.getSpecialRecyclingList(req);
        return R.ok(resp);
    }

    @ApiOperation(value = "专题回收站恢复/删除")
    @PostMapping("/recover")
    public R recover(@RequestBody @Validated SpecialRecyclingRecoverIn req) {
        return specialRecyclingService.recoverSpecial(req);

    }
}
