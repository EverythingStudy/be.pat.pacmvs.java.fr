package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.CustomPage;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.core.web.controller.BaseController;
import cn.staitech.fr.vo.project.*;
import cn.staitech.fr.service.ProjectMemberService;
import cn.staitech.sft.logaudit.annotation.EncryptResponse;
import cn.staitech.sft.logaudit.annotation.LogAudit;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


/**
 * @author mugw
 * @version 2.6.0
 * @description 项目成员管理
 * @date 2025/5/14 13:44:14
 */
@Api(tags = "项目配置-项目成员配置")
@RestController
@RequestMapping("/specialMember")
public class ProjectMemberController extends BaseController {
    @Resource
    private ProjectMemberService projectMemberService;

    @ApiOperation(value = "项目配置-项目成员表列表", notes = "获取当前项目的成员列表")
    @PostMapping("/list")
    public R<CustomPage<ProjectMemberPageVo>> list(@RequestBody ProjectMemberPageReq req) {
        CustomPage<ProjectMemberPageVo> resp = projectMemberService.getSpecialMemberList(req);
        return R.ok(resp);
    }

    @LogAudit
    @ApiOperation(value = "项目配置-项目成员删除", notes = "项目成员删除" ,tags = {"I18n"})
    @PostMapping("/remove")
    public R remove(@RequestBody ProjectMemberRemoveVo req) {
       return projectMemberService.removeMember(req);
    }

    @EncryptResponse
    @ApiOperation(value = "项目配置-项目成员详情", notes = "项目成员详情" ,tags = {"I18n"})
    @GetMapping("/detail/{memberId}")
    public R<ProjectMemberInfo> remove(@PathVariable("memberId") Long memberId) {
        return projectMemberService.detail(memberId);
    }


    @LogAudit
    @ApiOperation(value = "项目配置-项目成员表增加",tags = {"I18n"})
    @PostMapping("/addMember")
    public R addMember(@RequestBody @Validated ProjectMemberVo req) {
        return projectMemberService.addMember(req);
    }

}
