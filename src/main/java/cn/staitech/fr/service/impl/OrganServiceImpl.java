package cn.staitech.fr.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.staitech.fr.domain.Organ;
import cn.staitech.fr.service.OrganService;
import cn.staitech.fr.mapper.OrganMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.analysis.function.Identity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author 86186
* @description 针对表【tb_organ】的数据库操作Service实现
* @createDate 2025-05-30 17:01:38
*/
@Service
public class OrganServiceImpl extends ServiceImpl<OrganMapper, Organ>
    implements OrganService{
    @Override
    public Map<String, String> getCategory() {
        Map<String, String> map = new HashMap<String, String>();
        List<Organ> list = list();
        if (CollectionUtils.isNotEmpty(list)) {
            map = list.stream().collect(Collectors.toMap(item -> item.getOrganizationId().toString() + item.getOrganCode(), Organ::getName, (entity1, entity2) -> entity1));
        }
        return map;
    }
}




