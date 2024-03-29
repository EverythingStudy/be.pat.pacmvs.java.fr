package cn.staitech.fr.service.impl;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.WaxBlockInfo;
import cn.staitech.fr.mapper.WaxBlockInfoMapper;
import cn.staitech.fr.service.WaxBlockInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 蜡块编号明细 服务实现类
 * </p>
 *
 * @author author
 * @since 2024-03-29
 */
@Service
@Slf4j
public class WaxBlockInfoServiceImpl extends ServiceImpl<WaxBlockInfoMapper, WaxBlockInfo> implements WaxBlockInfoService {

    @Override
    public R<List<WaxBlockInfo>> waxPreview(Long id) {
        log.info("蜡块编号预览接口查询开始：");
        LambdaQueryWrapper<WaxBlockInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WaxBlockInfo::getWaxId, id);
        List<WaxBlockInfo> waxBlockInfos = this.baseMapper.selectList(wrapper);
        return R.ok(waxBlockInfos);
    }
}
