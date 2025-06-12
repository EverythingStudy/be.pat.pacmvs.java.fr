package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.CustomPage;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.core.web.controller.BaseController;
import cn.staitech.fr.vo.project.ProjectMemberVo;
import cn.staitech.fr.vo.project.ProjectMemberPageReq;
import cn.staitech.fr.vo.project.ProjectMemberPageVo;
import cn.staitech.fr.service.ProjectMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @ApiOperation(value = "项目配置-项目成员删除", notes = "项目成员删除")
    @GetMapping("/remove")
    public R remove(@RequestParam("memberId")@ApiParam(value = "memberId",name = "项目成员id") Long memberId) {
       return projectMemberService.removeMember(memberId);

    }


    @ApiOperation(value = "项目配置-项目成员表增加")
    @PostMapping("/addMember")
    public R addMember(@RequestBody @Validated ProjectMemberVo req) {
        return projectMemberService.addMember(req);
    }

}
