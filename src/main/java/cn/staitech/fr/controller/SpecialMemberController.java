package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.log.annotation.Log;
import cn.staitech.common.log.enums.BusinessType;
import cn.staitech.fr.domain.in.AddMemberIn;
import cn.staitech.fr.domain.in.SpecialMemberSelectIn;
import cn.staitech.fr.domain.out.SpecialMemberSelectOut;
import cn.staitech.fr.service.SpecialMemberService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
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


/**
 * @Author wudi
 * @Date 2024/4/1 13:41
 * @desc
 */
@Api(tags = "专题成员配置")
@RestController
@RequestMapping("/specialMember")
public class SpecialMemberController {
    @Autowired
    private SpecialMemberService specialMemberService;

    @ApiOperation(value = "项目成员表列表", notes = "获取当前项目的成员列表")
    @PostMapping("/list")
    public R<PageResponse<SpecialMemberSelectOut>> list(@RequestBody SpecialMemberSelectIn req) {
        PageResponse<SpecialMemberSelectOut> resp = specialMemberService.getSpecialMemberList(req);
        return R.ok(resp);
    }

    @ApiOperation(value = "专题成员删除", notes = "专题成员删除")
    @GetMapping("/remove")
    public R remove(@RequestParam("memberId")@ApiParam(value = "memberId",name = "专题成员id") Long memberId) {
       return specialMemberService.removeMember(memberId);

    }


    @ApiOperation(value = "专题成员表增加")
    @PostMapping("/addMember")
    public R addMember(@RequestBody @Validated AddMemberIn req) {
        return specialMemberService.addMember(req);
    }

}
