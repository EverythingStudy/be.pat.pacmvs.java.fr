package cn.staitech.fr.mapper;

import cn.staitech.common.core.domain.CustomPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.staitech.fr.domain.Project;
import cn.staitech.fr.vo.project.ProjectPageReq;
import cn.staitech.fr.vo.project.ProjectPageVo;
import org.apache.ibatis.annotations.Param;

/**
 * @author mugw
 * @version 2.6.0
 * @description 项目管理
 * @date 2025/5/14 13:44:14
 */
public interface ProjectMapper extends BaseMapper<Project> {

    CustomPage<ProjectPageVo> pageProject(CustomPage<ProjectPageVo> page, @Param("params") ProjectPageReq req);
}
