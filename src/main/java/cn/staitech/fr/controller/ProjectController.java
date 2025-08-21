package cn.staitech.fr.controller;

import cn.hutool.core.date.DateUtil;
import cn.staitech.common.core.domain.CustomPage;
import cn.staitech.common.core.domain.DateRangeReq;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.core.enums.UserStatus;
import cn.staitech.common.core.web.controller.BaseController;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.Constants;
import cn.staitech.fr.constant.DictData;
import cn.staitech.fr.domain.AccessProjectRecords;
import cn.staitech.fr.domain.Project;
import cn.staitech.fr.domain.ProjectLockLog;
import cn.staitech.fr.service.AccessViewRecordsService;
import cn.staitech.fr.vo.AccessProjectRecordsVo;
import cn.staitech.fr.vo.project.*;
import cn.staitech.fr.service.AccessProjectRecordsService;
import cn.staitech.fr.service.SpecialLockLogService;
import cn.staitech.fr.service.ProjectService;
import cn.staitech.fr.utils.LanguageUtils;
import cn.staitech.fr.vo.project.slide.ChangeControlGroupReq;
import cn.staitech.fr.vo.project.slide.GetControlGroupReq;
import cn.staitech.system.api.RemoteUserService;
import cn.staitech.system.api.domain.SysUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;


/**
 * @author mugw
 * @version 2.6.0
 * @description 项目管理
 * @date 2025/5/14 13:44:14
 */
@Api(value = "项目管理", tags =  {"项目管理","V2.6.0"})
@RestController
@RequestMapping("/project")
public class ProjectController extends BaseController {

    @Resource
    private ProjectService projectService;

    @Resource
    private AccessProjectRecordsService accessProjectRecordsService;

    @Resource
    private SpecialLockLogService specialLockLogService;

    @Resource
    private RemoteUserService remoteUserService;

    @Resource
    private AccessViewRecordsService accessViewRecordsService;

    @ApiOperation(value = "获取项目详情，添加项目访问记录",tags = {"V2.6.0"})
    @GetMapping("/info")
    public R<Project> info(@RequestParam("projectId") @ApiParam(name = "projectId", value = "项目id") Long projectId) {
        Long userId = SecurityUtils.getUserId();
        AccessProjectRecords record = AccessProjectRecords.builder().projectId(projectId).userId(userId).accessTime(new Date()).build();
        accessProjectRecordsService.saveAccessProjectRecords(record);
        return projectService.getInfoById(projectId);
    }

    @ApiOperation(value = "获取项目详情",tags = {"V2.6.0"})
    @GetMapping("/{projectId}")
    public R<Project> getProject(@PathVariable("projectId") Long projectId) {
        return projectService.getInfoById(projectId);
    }

    @ApiOperation(value = "近一个月访问项目情况", tags = {"V2.6.0"})
    @GetMapping("/accessProjectStatistics")
    public R<List<AccessProjectRecordsVo>> accessProjectStatistics()throws Exception{
        return accessProjectRecordsService.accessProjectStatistics();
    }

    @ApiOperation(value = "首页访问view信息")
    @PostMapping("/accessViewStatistics")
    public R accessViewStatistics(@RequestBody AccessViewStatisticsReq req) {
        return accessViewRecordsService.accessViewStatistics(req);
    }

    @ApiOperation(value = "项目列表分页查询", tags = {"V2.6.0"})
    @PostMapping("/page")
    public R<CustomPage<ProjectPageVo>> page(@RequestBody @Validated ProjectPageReq req) {
        return R.ok(projectService.pageProject(req, false));
    }

    @ApiOperation(value = "已归档项目分页查询", tags = {"V2.6.0"})
    @PostMapping("/pageArchived")
    public R<CustomPage<ProjectPageVo>> archivedList(@RequestBody @Validated ProjectPageReq req) {
        req.setStatus(Arrays.asList(Constants.STATUS_ARCHIVED));
        return R.ok(projectService.pageProject(req, false));
    }

    @ApiOperation(value = "回收站项目分页查询", tags = {"V2.6.0"})
    @PostMapping("/pageRecycle")
    public R<CustomPage<ProjectPageVo>> pageRecycle(@RequestBody @Validated ProjectPageReq req) {
        DateRangeReq expireTimeParams = req.getExpireTimeParams();
        if (expireTimeParams != null){
            Date beginTime = req.getExpireTimeParams().getBeginTime();
            if (beginTime != null){
                expireTimeParams.setBeginTime(DateUtil.offsetDay(beginTime, -30));
            }
            Date endTime = req.getExpireTimeParams().getEndTime();
            if (endTime != null){
                expireTimeParams.setEndTime(DateUtil.offsetDay(endTime, -30));
            }
        }
        return R.ok(projectService.pageProject(req, true));
    }

    @ApiOperation(value = "项目新增" , tags = {"V2.6.0"})
    @PostMapping("/add")
    public R add(@RequestBody @Validated ProjectVo req) {
        return projectService.addProject(req);
    }

    @ApiOperation(value = "项目修改" , tags = {"V2.6.0"})
    @PostMapping("/edit")
    public R edit(@RequestBody @Validated ProjectEditVo req) {
        return projectService.editProject(req);
    }

    @ApiOperation(value = "编辑项目状态", tags = {"V2.6.0"})
    @PostMapping("/editStatus" )
    public R editStatus(@Validated @RequestBody ProjectStatusVo req) {
        return projectService.editProjectStatus(req);
    }

    @ApiOperation(value = "项目删除", tags = {"V2.6.0"})
    @PostMapping("/remove/{projectId}")
    public R remove(@PathVariable("projectId") Long projectId) {
        return projectService.removeProject(projectId);
    }

    @ApiOperation(value = "染色类型列表", notes = "染色类型列表")
    @GetMapping("/colorType")
    public R<Map<Integer, String>> colorType() {
        Map<Integer, String> map;
        if (LanguageUtils.isEn()) {
            map = DictData.COLOR_TYPE_EN;
        } else {
            map = DictData.COLOR_TYPE;
        }
        return R.ok(map);
    }

    @ApiOperation(value = "试验类型列表", notes = "试验类型列表")
    @GetMapping("/trialType")
    public R<Map<Integer, String>> trialType() {
        Map<Integer, String> map;
        if (LanguageUtils.isEn()) {
            map = DictData.TRIAL_TYPE_EN;
        } else {
            map = DictData.TRIAL_TYPE;
        }
        return R.ok(map);
    }

    @ApiOperation(value = "项目状态列表", notes = "项目状态列表")
    @GetMapping("/projectStatus")
    public R<Map<Integer, String>> specialStatus(@Validated @RequestParam Boolean flag) {
        Map<Integer, String> map;
        if (LanguageUtils.isEn()) {
            map = DictData.SPECIAL_STATUS_EN;
            if (flag) {
                map = DictData.SPECIAL_STATUS_ARCHIVED_EN;
            }
        } else {
            map = DictData.SPECIAL_STATUS;
            if (flag) {
                map = DictData.SPECIAL_STATUS_ARCHIVED;
            }
        }
        return R.ok(map);
    }

    @ApiOperation(value = "项目锁定日志")
    @GetMapping("/getLockLog")
    public R<List<ProjectLockLog>> getLockLog(@RequestParam("projectId") @ApiParam(name = "projectId", value = "项目id") Long projectId) {
        //查询锁定记录
        List<ProjectLockLog> list = specialLockLogService.list(Wrappers.<ProjectLockLog>lambdaQuery().eq(ProjectLockLog::getProjectId, projectId)
                .orderByDesc(ProjectLockLog::getCreateTime));
        return R.ok(list);
    }

    @ApiOperation(value = "根据帐号查询昵称")
    @GetMapping("/getNickName")
    public R<String> getNickName(@RequestParam("userName") @ApiParam(name = "userName", value = "帐号名称") String userName) {
        String nickName = "";
        SysUser queryUser = new SysUser();
        queryUser.setUserName(userName);
        queryUser.setOrganizationId(SecurityUtils.getOrganizationId());
        queryUser.setDelFlag(cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL);
        queryUser.setStatus(UserStatus.OK.getCode());
        R<List<SysUser>> resp = remoteUserService.query(queryUser);
        if (cn.staitech.common.core.constant.Constants.SUCCESS.equals(resp.getCode())){
            List<SysUser> list = resp.getData();
            if (CollectionUtils.isNotEmpty(list)) {
                nickName = list.get(0).getNickName();
            }
        }
        return R.ok(nickName);
    }

    @ApiOperation(value = "回收站项目恢复")
    @PostMapping("/recycleProjectRecover/{projectId}")
    public R recycleProjectRecover(@PathVariable("projectId") Long projectId) {
        return projectService.recycleProjectRecover(projectId);
    }

    @ApiOperation(value = "回收站项目永久删除")
    @PostMapping("/recycleProjectDel/{projectId}")
    public R recycleProjectDel(@PathVariable("projectId") Long projectId) {
        return projectService.recycleProjectDel(projectId);
    }


    @ApiOperation(value = "更换对照组")
    @PostMapping("/changeControlGroup")
    public R<Boolean> changeControlGroup(@RequestBody @Validated ChangeControlGroupReq req) {
        return R.ok(this.projectService.changeControlGroup(req));
    }

    @ApiOperation(value = "查询已选对照组")
    @PostMapping("/getControlGroup")
    public R<String> getControlGroup(@RequestBody @Validated GetControlGroupReq req) {
        return R.ok(this.projectService.getControlGroup(req));
    }

}
