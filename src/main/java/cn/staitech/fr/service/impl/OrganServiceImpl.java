package cn.staitech.fr.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.domain.Organ;
import cn.staitech.fr.mapper.OrganMapper;
import cn.staitech.fr.service.IndicatorService;
import cn.staitech.fr.service.OrganService;
import cn.staitech.fr.service.StructureService;
import cn.staitech.fr.utils.LanguageUtils;


/**
 * @author: wangfeng
 * @create: 2023-09-10 13:10:18
 * @Description: 种属
 */
@Service
class OrganServiceImpl extends ServiceImpl<OrganMapper, Organ> implements OrganService {

    @Resource
    OrganMapper organMapper;

    @Resource
    private StructureService structureService;

    @Resource
    private IndicatorService indicatorService;


    @Override
    public Map<String, String> selectMap() {
        return select(false);
    }

    @Override
    public Map<String, String> selectMapEn() {
        return select(true);
    }

    /**
     * 根据种属编号获取脏器列表
     *
     * @param speciesCode
     * @return
     */
    @Override
    public List<Organ> getOrganBySpeciesId(String speciesCode) {
        Long organizationId = SecurityUtils.getLoginUser().getSysUser().getOrganizationId();
        Organ organQ = new Organ();
        organQ.setOrganizationId(organizationId);
        organQ.setSpeciesCode(speciesCode);
        List<Organ> list = organMapper.getOrganBySpeciesId(organQ);
        for (Organ organ : list) {
            // 中英文
            if (LanguageUtils.isEn()) {
                organ.setName(organ.getNameEn());
            }
        }
        return list;
    }

    public Map<String, String> select(boolean en) {
        List<Organ> list = organMapper.selectList();
        if (en) {
            return list.stream().collect(Collectors.toMap(item -> item.getOrganizationId().toString() + item.getSpeciesCode() + item.getOrganId(), Organ::getNameEn));
        } else {
            return list.stream().collect(Collectors.toMap(item -> item.getOrganizationId().toString() + item.getSpeciesCode() + item.getOrganId(), Organ::getName));
        }
    }

}
