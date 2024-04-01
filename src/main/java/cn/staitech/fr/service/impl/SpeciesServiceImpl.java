package cn.staitech.fr.service.impl;

import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.domain.Species;
import cn.staitech.fr.domain.out.SpeciesOut;
import cn.staitech.fr.mapper.SpeciesMapper;
import cn.staitech.fr.service.SpeciesService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 种属表 服务实现类
 * </p>
 *
 * @author author
 * @since 2024-03-29
 */
@Service
@Slf4j
public class SpeciesServiceImpl extends ServiceImpl<SpeciesMapper, Species> implements SpeciesService {

    @Override
    public List<Species> getSpeciesList() {
        log.info("种属下拉框接口查询开始：");
        LambdaQueryWrapper<Species> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Species::getOrganizationId, SecurityUtils.getLoginUser().getSysUser().getOrganizationId());
        List<Species> species = this.baseMapper.selectList(wrapper);
        return species;
    }
}
