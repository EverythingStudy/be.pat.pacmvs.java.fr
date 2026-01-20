package cn.staitech.fr.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.staitech.common.core.constant.SecurityConstants;
import cn.staitech.common.core.domain.CustomPage;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.core.utils.bean.BeanUtils;
import cn.staitech.common.redis.service.RedisService;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.constant.Constants;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.enmu.SpeciesTypeEnum;
import cn.staitech.fr.enums.ColorTypeEnum;
import cn.staitech.fr.enums.TrialTypeEnum;
import cn.staitech.fr.utils.SysRoleUtils;
import cn.staitech.fr.vo.project.*;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.service.SlideService;
import cn.staitech.fr.service.ProjectService;
import cn.staitech.fr.utils.MessageSource;
import cn.staitech.fr.utils.ProjectButtonGenerator;
import cn.staitech.fr.vo.project.slide.ChangeControlGroupReq;
import cn.staitech.fr.vo.project.slide.GetControlGroupReq;
import cn.staitech.fr.vo.project.slide.SlideInfo;
import cn.staitech.sft.logaudit.req.OperationObjectReq;
import cn.staitech.system.api.RemoteUserService;
import cn.staitech.system.api.domain.SysRole;
import cn.staitech.system.api.model.LoginUser;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Resource
    private SpeciesMapper speciesMapper;

    @Resource
    private RedisService redisService;
    @Resource
    private SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private AnnotationMapper annotationMapper;
    @Resource
    private RemoteUserService remoteUserService;
    @Resource
    private UserMapper userMapper;

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
            if (SysRoleUtils.isQualityAdmin()) {
                req.setStatus(Arrays.asList(Constants.STATUS_RUNNING));
            } else {
                req.setStatus(Arrays.asList(Constants.STATUS_PENDING, Constants.STATUS_RUNNING, Constants.STATUS_PAUSED, Constants.STATUS_COMPLETED));
            }
        }
        if (!isAdmin(SecurityUtils.getUserId())) {
            req.setOrganizationId(SecurityUtils.getOrganizationId());
        }
        if (isDelete) {
            req.setDelFlag(cn.staitech.common.core.constant.Constants.DEL_FLAG_DELETED);
        }
        baseMapper.pageProject(page, req);
        page.convert(this::renderProject);
        return page;
    }

    private ProjectPageVo renderProject(ProjectPageVo e) {
        ColorTypeEnum colorTypeEnum = ColorTypeEnum.getByCode(Integer.valueOf(e.getColorType()));
        e.setColorName(colorTypeEnum.getValue());
        e.setColorNameEn(colorTypeEnum.getValueEn());
        TrialTypeEnum trialTypeEnum = TrialTypeEnum.getByCode(Integer.valueOf(e.getTrialId()));
        e.setTrialType(trialTypeEnum.getValue());
        e.setTrialTypeEn(trialTypeEnum.getValueEn());
        e.setExpireTime(DateUtil.offsetDay(e.getUpdateTime(), 30));
        long count = projectMemberMapper.selectCount(Wrappers.<ProjectMember>lambdaQuery().eq(ProjectMember::getProjectId, e.getProjectId()).eq(ProjectMember::getUserId, SecurityUtils.getUserId()).eq(ProjectMember::getDelFlag, cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL));
        List<String> buttons = new ArrayList<>();

        boolean matchAdmin = SysRoleUtils.matchAdmin(SysRoleUtils.INTELL_ADMIN, SysRoleUtils.NUMBER_ADMIN);
        boolean qualityAdmin = SysRoleUtils.isQualityAdmin();

        if (SecurityUtils.isOrgAdmin() || (SecurityUtils.getUserId().equals(e.getPrincipal()) && matchAdmin)) {
            buttons = ProjectButtonGenerator.generateButtons(e.getStatus(), Constants.ROLE_OWNER);
            //这块可以用或者因为产品想让质量管理员可以看到详情按钮，即便我没有参与这个项目我也可以查看
        } else if ((count > 0 && matchAdmin) || qualityAdmin) {
            buttons = ProjectButtonGenerator.generateButtons(e.getStatus(), Constants.ROLE_MEMBER);
        } else {
            buttons = ProjectButtonGenerator.generateButtons(e.getStatus(), Constants.ROLE_OTHER);
        }
        ProjectButtonGenerator.removeButtonsByRole(buttons);
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
    //@Transactional(rollbackFor = Exception.class)
    public R addProject(ProjectVo req) {
        log.info("添加项目接口开始：");
        long archivedCount = count(Wrappers.<Project>lambdaQuery().eq(Project::getOrganizationId, SecurityUtils.getOrganizationId()).eq(Project::getDelFlag, cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL).and(wrapper -> wrapper.eq(Project::getProjectName, req.getProjectName()).or().eq(Project::getTopicId, req.getTopicId())).eq(Project::getStatus, Constants.STATUS_ARCHIVED));
        if (archivedCount > 0) {
            return R.fail(MessageSource.M("project.archived.cannot.create"));
        }

        long normalCount = count(Wrappers.<Project>lambdaQuery().eq(Project::getOrganizationId, SecurityUtils.getOrganizationId()).eq(Project::getDelFlag, cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL).eq(Project::getTopicId, req.getTopicId()));
        if (normalCount > 0) {
            return R.fail(MessageSource.M("topic.id.exists"));
        }

        normalCount = count(Wrappers.<Project>lambdaQuery().eq(Project::getOrganizationId, SecurityUtils.getOrganizationId()).eq(Project::getDelFlag, cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL).eq(Project::getProjectName, req.getProjectName()));
        if (normalCount > 0) {
            return R.fail(MessageSource.M("project.name.exists"));
        }

        Project project = new Project();
        BeanUtils.copyProperties(req, project);
        project.setCreateBy(SecurityUtils.getUserId());
        project.setCreateTime(new Date());
        project.setTopicName(topicMapper.selectById(project.getTopicId()).getTopicName());
        baseMapper.insert(project);
        List<OperationObjectReq> operationObjects = new ArrayList<>();
        OperationObjectReq operationObject = new OperationObjectReq();
        operationObject.setName("项目编号");
        operationObject.setNameEn("Project ID");
        operationObject.setValue(project.getProjectId() + project.getTopicName());
        operationObject.setValueEn(project.getProjectId() + project.getTopicName());
        operationObjects.add(operationObject);
        OperationObjectReq operationObject1 = new OperationObjectReq();
        operationObject1.setName("专题编号");
        operationObject1.setNameEn("Study ID");
        operationObject1.setValue(project.getProjectId() + project.getTopicName());
        operationObject1.setValueEn(project.getProjectId() + project.getTopicName());
        operationObjects.add(operationObject1);
        req.getLogAuditParams().setOperationObjects(operationObjects);
        //查询图像切片
        List<Image> images = imageMapper.selectList(Wrappers.<Image>lambdaQuery().eq(Image::getTopicId, req.getTopicId()).eq(Image::getStatus, Constants.IMAGE_STATUS_ENABLE).eq(Image::getOrganizationId, SecurityUtils.getOrganizationId()).eq(Image::getAnalyzeStatus, Constants.IMAGE_NAME_PARSE_SUCC));
        //初始化切片
        List<Slide> slides = new ArrayList<>();
        List<SlideInfo> slideInfos = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(images)) {
            for (Image image : images) {
                Slide slide = new Slide();
                slide.setCreateBy(SecurityUtils.getUserId());
                slide.setCreateTime(new Date());
                slide.setImageId(image.getImageId());
                slide.setProjectId(project.getProjectId());
                slides.add(slide);
                slideInfos.add(SlideInfo.builder()
                        .imageCode(image.getImageCode())
                        .animalCode(image.getAnimalCode())
                        .sexFlag(image.getSexFlag())
                        .createBy(SecurityUtils.getUserId())
                        .groupCode(image.getGroupCode())
                        .waxCode(image.getWaxCode())
                        .createTime(slide.getCreateTime()).build());
            }
        }
        slideService.saveBatch(slides);
        for (Slide slide : slides) {
            for (SlideInfo slideInfo : slideInfos) {
                if (slide.getImageId().equals(slideInfo.getImageId())) {
                    slideInfo.setSlideId(slide.getSlideId());
                }
            }
        }
        req.setSlideInfos(slideInfos);
        //添加项目成员
        addProjectMember(project.getProjectId(), req.getPrincipal());
        User user = userMapper.selectById(req.getPrincipal());
        R<LoginUser> loginUserResp = remoteUserService.getUserInfo(user.getUserName(), SecurityConstants.INNER);
        if (loginUserResp.getCode() == R.SUCCESS) {
            LoginUser loginUser = loginUserResp.getData();
            String roleNames = loginUser.getRoleObjs().stream()
                    .map(SysRole::getRoleName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
            req.setProjectMemberInfos(Collections.singletonList(ProjectMemberInfo.builder()
                    .userId(user.getUserId())
                    .sex(user.getSex())
                    .userName(user.getUserName()).nickName(user.getNickName())
                    .roleName(roleNames).phonenumber(user.getPhonenumber()).build()));
        }
        getSpecialAnnotationRel(project.getProjectId(), req.getPrincipal());
        req.setCreateTime(project.getCreateTime());
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

    public SpecialAnnotationRel getSpecialAnnotationRel(Long specialId, Long userId) {
        //缓存获取
        SpecialAnnotationRel cacheSpecialAnnotationRel = redisService.getCacheObject(CommonConstant.SPECIAL_ANNOTATION_REL + specialId);
        if (null == cacheSpecialAnnotationRel) {
            QueryWrapper<SpecialAnnotationRel> parQueryWrapperBy = new QueryWrapper<>();
            parQueryWrapperBy.eq("special_id", specialId);
            List<SpecialAnnotationRel> parList = specialAnnotationRelMapper.selectList(parQueryWrapperBy);
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(parList)) {
                redisService.setCacheObject(CommonConstant.SPECIAL_ANNOTATION_REL + specialId, parList.get(0), CommonConstant.SPECIAL_ANNOTATION_REL_CACHE_DAYS, TimeUnit.DAYS);
                return parList.get(0);
            } else {
                cacheSpecialAnnotationRel = new SpecialAnnotationRel();
                //查下可以使用的表
                QueryWrapper<SpecialAnnotationRel> userQueryWrapperBy = new QueryWrapper<>();
                userQueryWrapperBy.orderByDesc("sequence_number");
                userQueryWrapperBy.last("LIMIT 1");
                List<SpecialAnnotationRel> userPARList = specialAnnotationRelMapper.selectList(userQueryWrapperBy);
                if (org.apache.commons.collections4.CollectionUtils.isEmpty(userPARList)) {
                    //新建表-从1开始
                    synchronized (this) {
                        Long sequenceNumber = 1L;
                        cacheSpecialAnnotationRel = generateTable(cacheSpecialAnnotationRel, specialId, userId, sequenceNumber);
                        return cacheSpecialAnnotationRel;
                    }
                } else {
                    SpecialAnnotationRel pAnnoRel = userPARList.get(0);
                    Long currentSequenceNumber = pAnnoRel.getSequenceNumber();
                    if (null == currentSequenceNumber) {
                        synchronized (this) {
                            Long sequenceNumber = 1L;
                            cacheSpecialAnnotationRel = generateTable(cacheSpecialAnnotationRel, specialId, userId, sequenceNumber);
                            return cacheSpecialAnnotationRel;
                        }
                    }
                    //查下当前表的项目个数和记录条数，如果可以用继续，如果不可以就新建表
                    Annotation queryAnnotation = new Annotation();
                    queryAnnotation.setSequenceNumber(currentSequenceNumber);

                    Integer totalProjects = specialAnnotationRelMapper.selectTableSpecialCount(queryAnnotation);
                    Integer totalRecords = annotationMapper.selectTableRecordCount(queryAnnotation);
                    if (totalProjects >= CommonConstant.PROJECT_NUMBER_LIMIT || totalRecords >= CommonConstant.TABLE_RECORD_LIMIT) {
                        //新建表
                        Long sequenceNumber = currentSequenceNumber + 1;
                        synchronized (this) {
                            cacheSpecialAnnotationRel = generateTable(cacheSpecialAnnotationRel, specialId, userId, sequenceNumber);
                            return cacheSpecialAnnotationRel;
                        }
                    } else {
                        //3、insert 记录
                        cacheSpecialAnnotationRel.setSpecialId(specialId);
                        cacheSpecialAnnotationRel.setSequenceNumber(currentSequenceNumber);
                        cacheSpecialAnnotationRel.setCreateBy(userId);
                        cacheSpecialAnnotationRel.setCreateTime(new Date());
                        specialAnnotationRelMapper.insert(cacheSpecialAnnotationRel);
                        redisService.setCacheObject(CommonConstant.SPECIAL_ANNOTATION_REL + specialId, cacheSpecialAnnotationRel, CommonConstant.SPECIAL_ANNOTATION_REL_CACHE_DAYS, TimeUnit.DAYS);
                        return cacheSpecialAnnotationRel;
                    }
                }
            }
        } else {
            //已有表继续使用
            return cacheSpecialAnnotationRel;
        }
    }

    private SpecialAnnotationRel generateTable(SpecialAnnotationRel cacheSpecialAnnotationRel, Long specialId, Long userId, Long sequenceNumber) {
        Annotation annotation = new Annotation();
        annotation.setSequenceNumber(sequenceNumber);
        //先确认是否存在这个表了，如果存在就不新建表了
        Integer existTable = annotationMapper.selectExistTable(annotation);
        if (existTable == 0) {
            //1、Sequence
            annotationMapper.createTableSequence(annotation);
            //2、建表
            annotationMapper.createTable(annotation);
        }
        //3、insert 记录
        cacheSpecialAnnotationRel.setSpecialId(specialId);
        cacheSpecialAnnotationRel.setSequenceNumber(sequenceNumber);
        cacheSpecialAnnotationRel.setCreateBy(userId);
        cacheSpecialAnnotationRel.setCreateTime(new Date());
        specialAnnotationRelMapper.insert(cacheSpecialAnnotationRel);
        redisService.setCacheObject(CommonConstant.SPECIAL_ANNOTATION_REL + specialId, cacheSpecialAnnotationRel, CommonConstant.SPECIAL_ANNOTATION_REL_CACHE_DAYS, TimeUnit.DAYS);
        return cacheSpecialAnnotationRel;
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
            return R.fail(MessageSource.M("project.non.org.user"));
        }
        if (status == Constants.STATUS_COMPLETED) {
            return R.fail(MessageSource.M("project.completed.cannot.modify"));

        }
        if (status == Constants.STATUS_RUNNING) {
            return R.fail(MessageSource.M("project.running.cannot.modify"));
        }
        if (status == Constants.STATUS_PAUSED && !(SecurityUtils.getUserId() == entity.getPrincipal() || SecurityUtils.isOrgAdmin())) {
            return R.fail(MessageSource.M("project.no.permission"));
        }

        long count = count(Wrappers.<Project>lambdaQuery().eq(Project::getOrganizationId, req.getOrganizationId()).eq(Project::getDelFlag, cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL).eq(Project::getProjectName, req.getProjectName()).ne(Project::getProjectId, req.getProjectId()));
        if (count > 0) {
            return R.fail(MessageSource.M("EXISTS_SPECIAL_DATA"));
        }

        // 项目启动后，仅在暂停时可以修改基础信息部分的项目名称和负责人外，其他信息不允许修改
        Project project = new Project();
        if (status == Constants.STATUS_PAUSED) {
            project.setProjectId(req.getProjectId());
            // 项目名称
            project.setProjectName(req.getProjectName());
            // 负责人
            project.setPrincipal(req.getPrincipal());
            project.setUpdateBy(SecurityUtils.getUserId());
            project.setUpdateTime(new Date());
            baseMapper.updateById(project);
        } else {
            BeanUtils.copyProperties(req, project);
            project.setUpdateBy(SecurityUtils.getUserId());
            project.setUpdateTime(new Date());
            baseMapper.updateById(project);
        }
        // 添加项目成员
        Long memberCount = projectMemberMapper.selectCount(Wrappers.<ProjectMember>lambdaQuery().eq(ProjectMember::getProjectId, project.getProjectId()).eq(ProjectMember::getUserId, req.getPrincipal()).eq(ProjectMember::getDelFlag, cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL));
        if (memberCount == 0) {
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
            return R.fail(MessageSource.M("project.delete.pending.only"));
        }
        project.setDelFlag(cn.staitech.common.core.constant.Constants.DEL_FLAG_DELETED);
        project.setUpdateBy(SecurityUtils.getUserId());
        project.setUpdateTime(new Date());
        baseMapper.updateById(project);
        return R.ok(project);
    }

    /**
     * 项目状态按钮
     *
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
                //return R.fail("校验失败");
                return R.fail(MessageSource.M("project.validation.failed"));
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

        if (req.getStatus().equals(Constants.STATUS_ARCHIVED) && project.getStatus() != Constants.STATUS_COMPLETED) {
            return R.fail(MessageSource.M("project.archived.only.completed"));
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
        boolean matchAdmin = SysRoleUtils.matchAdmin(SysRoleUtils.INTELL_ADMIN, SysRoleUtils.NUMBER_ADMIN);
        Project project = baseMapper.selectById(projectId);
        project.setTopicName(topicMapper.selectById(project.getTopicId()).getTopicName());
        long count = projectMemberMapper.selectCount(Wrappers.<ProjectMember>lambdaQuery().eq(ProjectMember::getProjectId, projectId).eq(ProjectMember::getUserId, SecurityUtils.getUserId()).eq(ProjectMember::getDelFlag, cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL));
        List<String> buttons = new ArrayList<>();
        Integer status = project.getStatus();
        log.info("当前用户角色：{}", JSON.toJSONString(SecurityUtils.getRoles()));
        if (SecurityUtils.isOrgAdmin() || (SecurityUtils.getUserId().equals(project.getPrincipal()) && matchAdmin)) {
            buttons = ProjectButtonGenerator.generateButtons(status, Constants.ROLE_OWNER);
        } else if (count > 0 && matchAdmin) {
            buttons = ProjectButtonGenerator.generateButtons(status, Constants.ROLE_MEMBER);
        } else {
            buttons = ProjectButtonGenerator.generateButtons(status, Constants.ROLE_OTHER);
        }
        //在设置完成按钮之后过滤一下 在项目所选种属是“大鼠”、“小鼠”、“猴”、“犬”四个时显示，其他种属不显示任何按钮，即不提供任何AI辅助功能
        String speciesId = project.getSpeciesId();
        if (null != speciesId) {
            Integer speciesIds = Integer.parseInt(speciesId);
            if (!speciesIds.equals(SpeciesTypeEnum.RAT.getCode()) && !speciesIds.equals(SpeciesTypeEnum.MOUSE.getCode()) && !speciesIds.equals(SpeciesTypeEnum.DOG.getCode()) && !speciesIds.equals(SpeciesTypeEnum.MONKEY.getCode())) {
                ProjectButtonGenerator.filterButtons(buttons);
            }
        }
        ProjectButtonGenerator.removeButtonsByRole(buttons);
        project.setButtons(buttons);

        //增加种属名称
        if (null != project.getSpeciesId()) {
            LambdaQueryWrapper<Species> lambdaQueryWrapper = Wrappers.<Species>lambdaQuery()
                    .eq(Species::getSpeciesId, project.getSpeciesId())
                    .eq(Species::getOrganizationId, SecurityUtils.getOrganizationId());
            Species species = speciesMapper.selectOne(lambdaQueryWrapper);
            project.setSpeciesName(species.getName());
        }
        //ai是否执行
        boolean isAiTrained = slideService.checkAiExecuted(projectId);
        project.setIsAiTrained(isAiTrained);
        return R.ok(project);
    }

    /**
     * 项目删除
     *
     * @param projectId
     * @return
     */
    @Override
    public R recycleProjectDel(Long projectId) {
        int success = baseMapper.updateById(Project.builder().projectId(projectId).isPermanentDel(true).build());
        //删除切片、标注数据 todo
        return R.ok(MessageSource.M("project.delete.success"));
    }

    /**
     * 项目恢复
     *
     * @param projectId
     * @return
     */
    @Override
    public R recycleProjectRecover(Long projectId) {
        Project project = getById(projectId);
        //校验项目编号
        long count = count(Wrappers.<Project>lambdaQuery().eq(Project::getTopicId, project.getTopicId()).eq(Project::getDelFlag, cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL).eq(Project::getOrganizationId, SecurityUtils.getOrganizationId()));
        if (count > 0) {
            return R.fail(MessageSource.M("topic.id.exists.restore"));
        }
        //校验项目名称
        count = count(Wrappers.<Project>lambdaQuery().eq(Project::getProjectName, project.getProjectName()).eq(Project::getDelFlag, cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL).eq(Project::getOrganizationId, SecurityUtils.getOrganizationId()));
        if (count > 0) {
            return R.fail(MessageSource.M("project.name.exists.restore"));
        }
        project.setDelFlag(cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL);
        int success = baseMapper.updateById(project);
        return R.ok(MessageSource.M("project.restore.success"));
    }

    @Override
    public Boolean changeControlGroup(ChangeControlGroupReq req) {
        Project project = getById(req.getProjectId());
        project.setControlGroup(req.getControlGroup());
        return baseMapper.updateById(project) > 0;
    }

    @Override
    public String getControlGroup(GetControlGroupReq req) {
        Project project = getById(req.getProjectId());
        return project.getControlGroup() == null ? "" : project.getControlGroup();
    }
}
