package cn.staitech.fr.service.impl;

import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Structure;
import cn.staitech.fr.mapper.StructureMapper;
import cn.staitech.fr.service.StructureService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: wangfeng
 * @create: 2023-09-10 13:10:18
 * @Description: 种属
 */
@Service
class StructureServiceImpl extends ServiceImpl<StructureMapper, Structure> implements StructureService {
    @Resource
    StructureMapper structureMapper;

    @Override
    public Map<String, String> selectMap() {
        return select(false);
    }

    @Override
    public Map<String, Integer> selectStructureSizeMap() {
        return selectStructureSize();
    }

    private Map<String, Integer> selectStructureSize() {
        List<Structure> list = structureMapper.selectList(new Structure());
        return list.stream().collect(
                HashMap::new,
                (m, node) -> m.put(node.getOrganizationId().toString() + node.getStructureId(), node.getStructureSize()),
                HashMap::putAll
        );
    }

    @Override
    public Map<String, String> selectMapEn() {
        return select(true);
    }

    @Override
    public List<Structure> getStructureList(String speciesId, String organId) {
        Structure structure = new Structure();
        structure.setSpeciesId(speciesId);
        structure.setOrganId(organId);
        structure.setType(CommonConstant.STRUCTURE_RO);
        structure.setOrganizationId(SecurityUtils.getLoginUser().getSysUser().getOrganizationId());
        List<Structure> list = structureMapper.selectList(structure);
        if (list.size() == 0) {
            Structure obj = new Structure();
            obj.setName("无关联");
            obj.setNameEn("Unrelated");
            obj.setSpeciesId(speciesId);
            obj.setStructureId(organId);
            list.add(obj);
        }
        return list;
    }

    @Override
    public Structure getOneStructure(String speciesId, String organId, String structureId) {
        Structure structure = new Structure();
        structure.setSpeciesId(speciesId);
        structure.setOrganId(organId);
        structure.setStructureId(structureId);
        structure.setOrganizationId(SecurityUtils.getLoginUser().getSysUser().getOrganizationId());
        QueryWrapper<Structure> queryWrapper = new QueryWrapper<>(structure);
        Structure structureResp = structureMapper.selectOne(queryWrapper);
        return structureResp;
    }

    @Override
    public List<Structure> getListByStructureId(String structureId) {
        Structure structure = new Structure();
        structure.setStructureId(structureId);
        structure.setOrganizationId(SecurityUtils.getLoginUser().getSysUser().getOrganizationId());
        List<Structure> list = structureMapper.selectList(structure);
        return list;
    }

    public Map<String, String> select(boolean en) {
        List<Structure> list = structureMapper.selectList(new Structure());
        if (en) {
            // 20231222
            return list.stream().collect(
                    HashMap::new,
                    (m, node) -> m.put(node.getOrganizationId().toString() + node.getSpeciesId() + node.getOrganId() + node.getStructureId(), node.getNameEn()),
                    HashMap::putAll
            );
        } else {
            return list.stream().collect(
                    HashMap::new,
                    (m, node) -> m.put(node.getOrganizationId().toString() + node.getSpeciesId() + node.getOrganId() + node.getStructureId(), node.getName()),
                    HashMap::putAll
            );
        }
    }
}
