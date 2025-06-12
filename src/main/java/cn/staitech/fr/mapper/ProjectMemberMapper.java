package cn.staitech.fr.mapper;

import cn.staitech.common.core.domain.CustomPage;
import cn.staitech.fr.domain.ProjectMember;
import cn.staitech.fr.vo.project.ProjectMemberPageReq;
import cn.staitech.fr.vo.project.ProjectMemberPageVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 项目成员表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2024-03-29
 */
public interface ProjectMemberMapper extends BaseMapper<ProjectMember> {

    CustomPage<ProjectMemberPageVo> getProjectMemberList(CustomPage page, @Param("params") ProjectMemberPageReq req);
}

