package cn.staitech.fr.service;

import cn.staitech.common.core.domain.CustomPage;
import cn.staitech.fr.vo.project.ProjectEditVo;
import cn.staitech.fr.vo.project.ProjectPageReq;
import cn.staitech.fr.vo.project.ProjectStatusVo;
import cn.staitech.fr.vo.project.ProjectVo;
import cn.staitech.fr.vo.project.slide.ChangeControlGroupReq;
import com.baomidou.mybatisplus.extension.service.IService;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.Project;
import cn.staitech.fr.vo.project.ProjectPageVo;

/**
 * <p>
 * 项目表 服务类
 * </p>
 *
 * @author author
 * @since 2024-03-29
 */
public interface ProjectService extends IService<Project> {

    CustomPage<ProjectPageVo> pageProject(ProjectPageReq req, Boolean isDelete);

    R addProject(ProjectVo req);

    R editProject(ProjectEditVo req);

    R removeProject(Long projectId);

    R editProjectStatus(ProjectStatusVo req);

    R<Project> getInfoById(Long specialId);

    R recycleProjectDel(Long projectId);

    R recycleProjectRecover(Long projectId);

    Boolean changeControlGroup(ChangeControlGroupReq req);

}
