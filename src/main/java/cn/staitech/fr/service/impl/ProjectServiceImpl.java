package cn.staitech.fr.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.staitech.common.core.domain.CustomPage;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.core.utils.bean.BeanUtils;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.Constants;
import cn.staitech.fr.constant.DictData;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.vo.project.ProjectPageVo;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.service.SlideService;
import cn.staitech.fr.service.ProjectService;
import cn.staitech.fr.utils.MessageSource;
import cn.staitech.fr.utils.ProjectButtonGenerator;
import cn.staitech.fr.vo.project.ProjectEditVo;
import cn.staitech.fr.vo.project.ProjectPageReq;
import cn.staitech.fr.vo.project.ProjectStatusVo;
import cn.staitech.fr.vo.project.ProjectVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.*;

import static cn.staitech.common.security.utils.SecurityUtils.isAdmin;


/**
 * @author mugw
 * @version 2.6.0
 * @description 项目管理
 * @date 2025/5/14 13:44:14
 */
@Service
@Slf4j
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    @Resource
    private ImageMapper imageMapper;

    @Resource
    private SlideService slideService;


    @Resource
    private TopicMapper topicMapper;

    @Resource
    private ProjectMemberMapper projectMemberMapper;

    /**
     * 分页查询项目列表
     *
     * @param req
     * @param isDelete
     * @return
     */
    @Override
    public CustomPage<ProjectPageVo> pageProject(ProjectPageReq req, Boolean isDelete) {
        CustomPage<ProjectPageVo> page = new CustomPage<>(req);
        // 状态查询条件为空时，默认赋值(0-待启动，1-进行中，2-暂停，3-已完成)
        if (CollectionUtils.isEmpty(req.getStatus())) {
            req.setStatus(Arrays.asList(Constants.STATUS_PENDING, Constants.STATUS_RUNNING, Constants.STATUS_PAUSED, Constants.STATUS_COMPLETED));
        }
        if(!isAdmin(SecurityUtils.getUserId())){
            req.setOrganizationId(SecurityUtils.getOrganizationId());
        }
        if(isDelete){
            req.setDelFlag(cn.staitech.common.core.constant.Constants.DEL_FLAG_DELETED);
        }
        baseMapper.pageProject(page, req);
        page.convert(this::renderProject);
        return page;
    }

    private ProjectPageVo renderProject(ProjectPageVo e) {
        e.setColorName(DictData.COLOR_TYPE.get(Integer.valueOf(e.getColorType())));
        e.setColorNameEn(DictData.COLOR_TYPE_EN.get(Integer.valueOf(e.getColorType())));
        e.setTrialType(DictData.TRIAL_TYPE.get(e.getTrialId()));
        e.setTrialTypeEn(DictData.TRIAL_TYPE_EN.get(e.getTrialId()));
        e.setExpireTime(DateUtil.offsetDay(e.getExpireTime(), 30));
        long count = projectMemberMapper.selectCount(Wrappers.<ProjectMember>lambdaQuery().eq(ProjectMember::getProjectId, e.getProjectId())
                .eq(ProjectMember::getUserId, SecurityUtils.getUserId())
                .eq(ProjectMember::getDelFlag, cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL));
        List<String> buttons = new ArrayList<>();
        if (SecurityUtils.isOrgAdmin() || SecurityUtils.getUserId() == e.getPrincipal()){
            buttons = ProjectButtonGenerator.generateButtons(e.getStatus(), Constants.ROLE_OWNER);
        }else if(count>0){
            buttons = ProjectButtonGenerator.generateButtons(e.getStatus(), Constants.ROLE_MEMBER);
        }else{
            buttons = ProjectButtonGenerator.generateButtons(e.getStatus(), Constants.ROLE_OTHER);
        }
        e.setButtons(buttons);
        return e;
    }


    /**
     * 新增项目数据
     *
     * @param req
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R addProject(ProjectVo req) {
        log.info("添加项目接口开始：");
        long archivedCount = count(Wrappers.<Project>lambdaQuery().eq(Project::getOrganizationId, SecurityUtils.getOrganizationId())
                .eq(Project::getDelFlag, cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL)
                .and(wrapper -> wrapper.eq(Project::getProjectName, req.getProjectName()).or().eq(Project::getTopicId, req.getTopicId()))
                .eq(Project::getStatus, Constants.STATUS_ARCHIVED));
        if (archivedCount > 0) {
            return R.fail("该项目已归档，不能重复创建项目，请选择其他项目");
        }

        long normalCount = count(Wrappers.<Project>lambdaQuery().eq(Project::getOrganizationId, SecurityUtils.getOrganizationId())
                .eq(Project::getDelFlag, cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL)
                .eq(Project::getTopicId, req.getTopicId()));
        if (normalCount > 0) {
            return R.fail("专题编号已存在，请重新输入！");
        }

        normalCount = count(Wrappers.<Project>lambdaQuery().eq(Project::getOrganizationId, SecurityUtils.getOrganizationId())
                .eq(Project::getDelFlag, cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL)
                .eq(Project::getProjectName, req.getProjectName()));
        if (normalCount > 0) {
            return R.fail("项目名称已存在，请重新输入！");
        }

        Project project = new Project();
        BeanUtils.copyProperties(req, project);
        project.setCreateBy(SecurityUtils.getUserId());
        project.setCreateTime(new Date());
        baseMapper.insert(project);

        //查询图像切片
        List<Image> images = imageMapper.selectList(Wrappers.<Image>lambdaQuery().eq(Image::getTopicId, req.getTopicId())
                .eq(Image::getStatus, Constants.IMAGE_STATUS_ENABLE)
                .eq(Image::getOrganizationId, SecurityUtils.getOrganizationId())
                .eq(Image::getAnalyzeStatus, Constants.IMAGE_NAME_PARSE_SUCC));
        //初始化切片
        List<Slide> slides = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(images)) {
            for (Image image : images) {
                Slide slide = new Slide();
                slide.setCreateBy(SecurityUtils.getUserId());
                slide.setCreateTime(new Date());
                slide.setImageId(image.getImageId());
                slide.setProjectId(project.getProjectId());
                slides.add(slide);
            }
        }
        slideService.saveBatch(slides);

        //添加项目成员
        addProjectMember(project.getProjectId(), req.getPrincipal());
        return R.ok(project);
    }

    private void addProjectMember(Long projectId, Long userId) {
        ProjectMember specialMember = new ProjectMember();
        specialMember.setProjectId(projectId);
        specialMember.setUserId(userId);
        specialMember.setOrganizationId(SecurityUtils.getOrganizationId());
        specialMember.setCreateBy(SecurityUtils.getUserId());
        specialMember.setCreateTime(new Date());
        projectMemberMapper.insert(specialMember);
    }

    /**
     * 编辑项目数据
     *
     * @param req
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public R editProject(ProjectEditVo req) {
        log.info("项目编辑接口开始：");
        Project entity = baseMapper.selectById(req.getProjectId());
        Integer status = entity.getStatus();
        if (entity.getOrganizationId() != SecurityUtils.getOrganizationId()) {
            return R.fail("非机构内用户不可以对该项目进行配置");
        }
        if (status == Constants.STATUS_COMPLETED) {
            return R.fail("项目状态为“完成”时，任何用户不能再配置项目任何信息");
        }
        if (status == Constants.STATUS_RUNNING) {
            return R.fail("项目状态为“进行中”时，不能配置项目基础信息");
        }
        if (status == Constants.STATUS_PAUSED && !(SecurityUtils.getUserId() == entity.getPrincipal() || SecurityUtils.isOrgAdmin())) {
            return R.fail("项目状态为“暂停”时，机构管理员和项目负责人可以配置项目基础信息");
        }

        long count = count(Wrappers.<Project>lambdaQuery().eq(Project::getOrganizationId, req.getOrganizationId())
                .eq(Project::getDelFlag, cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL)
                .eq(Project::getProjectName, req.getProjectName())
                .ne(Project::getProjectId, req.getProjectId()));
        if (count > 0) {
            return R.fail(MessageSource.M("EXISTS_SPECIAL_DATA"));
        }
        Project project = new Project();
        BeanUtils.copyProperties(req, project);
        project.setUpdateBy(SecurityUtils.getUserId());
        project.setUpdateTime(new Date());
        baseMapper.updateById(project);//添加项目成员
        Long memberCount = projectMemberMapper.selectCount(Wrappers.<ProjectMember>lambdaQuery().eq(ProjectMember::getProjectId, project.getProjectId()).eq(ProjectMember::getUserId, req.getPrincipal()));
        if (memberCount == 0){
            addProjectMember(project.getProjectId(), req.getPrincipal());
        }
        return R.ok(project);
    }

    /**
     * 删除项目数据
     *
     * @param projectId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R removeProject(Long projectId) {

        //判断项目下是否存在切片数据
        LambdaQueryWrapper<Slide> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Slide::getProjectId, projectId);
        queryWrapper.eq(Slide::getDelFlag, cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL);
        if (slideService.count(queryWrapper) > 0) {
            return R.fail(MessageSource.M("EXISTS_SLIDE_DATA"));
        }

        Project project = getById(projectId);
        if (project == null) {
            return R.fail(MessageSource.M("DATA_DOES_NOT_EXIST"));
        }
        if (!project.getStatus().equals(Constants.STATUS_PENDING)) {
            return R.fail("只有当状态为待启动时，可以删除");
        }
        project.setDelFlag(cn.staitech.common.core.constant.Constants.DEL_FLAG_DELETED);
        project.setUpdateBy(SecurityUtils.getUserId());
        project.setUpdateTime(new Date());
        baseMapper.updateById(project);
        return R.ok(project);
    }

    /**
     * 项目状态按钮
     * @param req
     * @return
     */
    @Override
    public R editProjectStatus(ProjectStatusVo req) {
        log.info("项目状态按钮接口开始：");
        Project project = baseMapper.selectById(req.getProjectId());
        //启动条件判断
        if (req.getStatus().equals(Constants.STATUS_RUNNING)) {
            //校验用户名、密码
            /*boolean res = userLoginVerify(req.getUserName(), req.getPwd(), req);
            if (!res) {
                return R.fail("校验失败");
            }*/
            int status = project.getStatus();
            if (status == Constants.STATUS_PENDING) {
                //判断项目下是否存在切片数据
                long slides = slideService.count(Wrappers.<Slide>lambdaQuery().eq(Slide::getProjectId, req.getProjectId()).eq(Slide::getDelFlag, cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL));
                if (slides == 0) {
                    return R.fail(MessageSource.M("NO_SLIDE_DATA_CANNOT_START"));
                }
            }
        }

        if (req.getStatus().equals(Constants.STATUS_ARCHIVED) && project.getStatus() != Constants.STATUS_COMPLETED){
            return R.fail("只有已完成才允许归档");
        }
        project.setUpdateTime(new Date());
        project.setUpdateBy(SecurityUtils.getUserId());
        project.setStatus(req.getStatus());
        baseMapper.updateById(project);
        return R.ok();
    }

    @Override
    public R<Project> getInfoById(Long projectId) {
        log.info("智能阅片项目详情接口开始：");
        Project project = baseMapper.selectById(projectId);
        project.setTopicName(topicMapper.selectById(project.getTopicId()).getTopicName());
        long count = projectMemberMapper.selectCount(Wrappers.<ProjectMember>lambdaQuery().eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, SecurityUtils.getUserId())
                .eq(ProjectMember::getDelFlag, cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL));
        List<String> buttons = new ArrayList<>();
        Integer status = project.getStatus();
        if (SecurityUtils.isOrgAdmin() || SecurityUtils.getUserId() == project.getPrincipal()){
            buttons = ProjectButtonGenerator.generateButtons(status, Constants.ROLE_OWNER);
        }else if(count>0){
            buttons = ProjectButtonGenerator.generateButtons(status, Constants.ROLE_MEMBER);
        }else{
            buttons = ProjectButtonGenerator.generateButtons(status, Constants.ROLE_OTHER);
        }
        project.setButtons(buttons);
        return R.ok(project);
    }

    /**
     * 项目删除
     * @param projectId
     * @return
     */
    @Override
    public R recycleProjectDel(Long projectId) {
        int success = baseMapper.updateById(Project.builder().projectId(projectId).isPermanentDel(true).build());
        //删除切片、标注数据 todo
        return R.ok("删除成功");
    }

    /**
     * 项目恢复
     * @param projectId
     * @return
     */
    @Override
    public R recycleProjectRecover(Long projectId) {
        Project project = getById(projectId);
        //校验项目编号
        long count = count(Wrappers.<Project>lambdaQuery().eq(Project::getTopicId,project.getTopicId()).eq(Project::getDelFlag, cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL)
                .eq(Project::getOrganizationId, SecurityUtils.getOrganizationId()));
        if (count > 0) {
            return R.fail("专题编号已存在，禁止恢复");
        }
        //校验项目名称
        count = count(Wrappers.<Project>lambdaQuery().eq(Project::getProjectName,project.getProjectName()).eq(Project::getDelFlag, cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL)
                .eq(Project::getOrganizationId, SecurityUtils.getOrganizationId()));
        if (count > 0) {
            return R.fail("专题名称已存在，禁止恢复");
        }
        project.setDelFlag(cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL);
        int success = baseMapper.updateById(project);
        return R.ok("恢复成功");
    }

}
