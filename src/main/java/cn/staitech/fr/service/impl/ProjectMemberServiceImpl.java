package cn.staitech.fr.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;

import cn.staitech.common.core.domain.CustomPage;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.utils.MessageSource;
import cn.staitech.system.api.RemoteAnnotationService;
import cn.staitech.system.api.domain.biz.CheckUserOperation;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.Constants;
import cn.staitech.fr.vo.project.ProjectMemberVo;
import cn.staitech.fr.vo.project.ProjectMemberPageReq;
import cn.staitech.fr.vo.project.ProjectMemberPageVo;
import cn.staitech.fr.service.ProjectMemberService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author mugw
 * @version 2.6.0
 * @description 项目成员管理
 * @date 2025/5/14 13:44:14
 */
@Service
@Slf4j
public class ProjectMemberServiceImpl extends ServiceImpl<ProjectMemberMapper, ProjectMember> implements ProjectMemberService {

    @Resource
    private SlideMapper slideMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private RemoteAnnotationService remoteAnnotationService;

    @Override
    public CustomPage<ProjectMemberPageVo> getSpecialMemberList(ProjectMemberPageReq req) {
        log.info("项目用户列表查询接口开始：");
        CustomPage<ProjectMemberPageVo> page = new CustomPage<>(req);
        baseMapper.getProjectMemberList(page,req);
        return page;

    }

    @Override
    public R removeMember(Long memberId) {
        log.info("项目成员删除接口开始：");
        //校验用户操作信息
        ProjectMember member = baseMapper.selectById(memberId);
        R validationResult = validateProjectStatus(member.getProjectId());
        if (validationResult != null) {
            return validationResult;
        }
        Long projectId = member.getProjectId();
        Long memberUserId = member.getUserId();
        //查询是否有标注信息
        LambdaQueryWrapper<Slide> slideWrapper = new LambdaQueryWrapper<>();
        slideWrapper.eq(Slide::getProjectId, projectId);
        slideWrapper.eq(Slide::getDelFlag, cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL);
        List<Slide> slideList = slideMapper.selectList(slideWrapper);
        if(CollectionUtils.isNotEmpty(slideList)) {
            List<Long> slideIdList =  slideList.stream().map(Slide::getSlideId).collect(Collectors.toList());
            CheckUserOperation queryUserOperation = new CheckUserOperation();
            queryUserOperation.setUserId(memberUserId);
            queryUserOperation.setSlideId(slideIdList);
            try {
                R<Boolean> resp = remoteAnnotationService.checkUserOperation(queryUserOperation);
                if(resp.getData()){
                    return R.fail(MessageSource.M("DATA_CANNOT_EDITED_OR_DELETED"));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        Project project = projectMapper.selectById(member.getProjectId());
        if (project.getPrincipal() == memberUserId){
            return R.fail(MessageSource.M("project.member.principal.cannot.delete"));
        }
        ProjectMember specialMember = new ProjectMember();
        specialMember.setMemberId(memberId);
        specialMember.setDelFlag(cn.staitech.common.core.constant.Constants.DEL_FLAG_DELETED);
        this.updateById(specialMember);
        return R.ok();

    }

    @Override
    public R addMember(ProjectMemberVo req) {
        log.info("项目成员添加接口开始：");
        R validationResult = validateProjectStatus(req.getProjectId());
        if (validationResult != null) {
            return validationResult;
        }
        //查询项目下所有成员
        List<ProjectMember> projectMembers = list(Wrappers.<ProjectMember>lambdaQuery().eq(ProjectMember::getProjectId, req.getProjectId()).eq(ProjectMember::getDelFlag, cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL));
        Map<Long, Long> projectMemberMap = projectMembers.stream()
                .collect(Collectors.toMap(
                        ProjectMember::getUserId,
                        ProjectMember::getProjectId,
                        (existing, replacement) -> existing // 或者 (existing, replacement) -> replacement，根据业务需求选择
                ));        List<ProjectMember> collect = new ArrayList<>();
        for (Long e : req.getUserId()) {
            if (projectMemberMap.containsKey(e)) {
                continue;
            }
            ProjectMember specialMember = new ProjectMember();
            specialMember.setProjectId(req.getProjectId());
            specialMember.setUserId(e);
            specialMember.setCreateBy(SecurityUtils.getUserId());
            specialMember.setCreateTime(new Date());
            collect.add(specialMember);
        }
        saveBatch(collect);
        return R.ok();
    }

    /**
     * 验证项目状态是否允许配置
     *
     * @param projectId 项目对象
     * @return 如果状态不合法，返回错误 R.fail；否则继续执行
     */
    private R validateProjectStatus(Long projectId) {
        Project special = projectMapper.selectById(projectId);
        Integer status = special.getStatus();

        if (status == Constants.STATUS_COMPLETED) {
            return R.fail(MessageSource.M("project.completed.cannot.modify"));
        }

        if (status == Constants.STATUS_PAUSED
                && !(SecurityUtils.getUserId() == special.getPrincipal() || SecurityUtils.isOrgAdmin())) {
            return R.fail(MessageSource.M("project.no.permission"));
        }

        return null; // 表示通过校验
    }

}

