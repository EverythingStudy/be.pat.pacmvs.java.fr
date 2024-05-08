package cn.staitech.fr.mapper;


import cn.staitech.fr.domain.OrganizationIdName;

import java.util.List;

/**
 * @author gjt
 */
public interface OrganizationMapper {

    /**
     * 查询组织表
     *
     * @return
     */
    List<OrganizationIdName> selectIdNameList();

}