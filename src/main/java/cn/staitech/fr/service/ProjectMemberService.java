package cn.staitech.fr.service;

import cn.staitech.common.core.domain.CustomPage;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.ProjectMember;
import cn.staitech.fr.vo.project.ProjectMemberVo;
import cn.staitech.fr.vo.project.ProjectMemberPageReq;
import cn.staitech.fr.vo.project.ProjectMemberPageVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author mugw
 * @version 2.6.0
 * @description 项目成员管理
 * @date 2025/5/14 13:44:14
 */
public interface ProjectMemberService extends IService<ProjectMember> {

    CustomPage<ProjectMemberPageVo> getSpecialMemberList(ProjectMemberPageReq req);

    R removeMember(Long memberId);

    R addMember(ProjectMemberVo req);
}
