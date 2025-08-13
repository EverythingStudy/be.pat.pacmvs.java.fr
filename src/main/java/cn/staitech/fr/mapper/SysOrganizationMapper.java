package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.OrganizationIdName;
import cn.staitech.fr.domain.SysOrganization;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author admin
* @description 针对表【sys_organization(机构表)】的数据库操作Mapper
* @createDate 2025-08-12 13:50:34
* @Entity generator.domain.SysOrganization
*/
public interface SysOrganizationMapper extends BaseMapper<SysOrganization> {
    /**
     * 查询组织表
     *
     * @return
     */
    List<OrganizationIdName> selectIdNameList();

    String getOrganizationName(Long organizationId);
}




