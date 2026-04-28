package cn.staitech.fr.service.impl;

import cn.staitech.fr.domain.Structure;
import cn.staitech.fr.mapper.StructureMapper;
import cn.staitech.fr.service.StructureService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 86186
 * @description 针对表【tb_structure】的数据库操作Service实现
 * @createDate 2025-05-30 17:01:39
 */
@Service
public class StructureServiceImpl extends ServiceImpl<StructureMapper, Structure> implements StructureService {
    @Resource
    StructureMapper structureMapper;

    @Override
    public Map<String, Integer> selectStructureSizeMap() {
        return selectStructureSize();
    }

    private Map<String, Integer> selectStructureSize() {
        List<Structure> list = structureMapper.queryList(new Structure());
        return list.stream().collect(HashMap::new, (m, node) -> m.put(node.getOrganizationId().toString() + node.getStructureId(), node.getStructureSize()), HashMap::putAll);
    }
}




