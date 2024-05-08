package cn.staitech.fr.service.impl;


import cn.staitech.fr.domain.OrganizationIdName;
import cn.staitech.fr.mapper.OrganizationMapper;
import cn.staitech.fr.service.OrganizationService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 查询组织表Map .
 *
 * @author staitech
 */
@Service
public class OrganizationServiceImpl implements OrganizationService {

    @Resource
    private OrganizationMapper organizationMapper;

    @Override
    public Map<Long, String> selectMap() {
        List<OrganizationIdName> list = organizationMapper.selectIdNameList();
        Map<Long, String> map = list.stream()
                .collect(Collectors.toMap(OrganizationIdName::getOrganizationId, OrganizationIdName::getOrganizationName));
        return map;
    }

}
