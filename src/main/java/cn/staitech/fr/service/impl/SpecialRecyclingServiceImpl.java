package cn.staitech.fr.service.impl;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.constant.Container;
import cn.staitech.fr.domain.Special;
import cn.staitech.fr.domain.SpecialRecycling;
import cn.staitech.fr.domain.in.SpecialRecyclingListQueryIn;
import cn.staitech.fr.domain.in.SpecialRecyclingRecoverIn;
import cn.staitech.fr.domain.out.SpecialListQueryOut;
import cn.staitech.fr.domain.out.SpecialRecyclingListQueryOut;
import cn.staitech.fr.mapper.SpecialMapper;
import cn.staitech.fr.mapper.SpecialRecyclingMapper;
import cn.staitech.fr.service.SpecialRecyclingService;
import cn.staitech.fr.service.SpecialService;
import cn.staitech.system.api.domain.SysUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 专题回收站表 服务实现类
 * </p>
 *
 * @author author
 * @since 2024-03-29
 */
@Service
@Slf4j
public class SpecialRecyclingServiceImpl extends ServiceImpl<SpecialRecyclingMapper, SpecialRecycling> implements SpecialRecyclingService {
    @Resource
    private SpecialMapper specialMapper;

    @Override
    public PageResponse<SpecialRecyclingListQueryOut> getSpecialRecyclingList(SpecialRecyclingListQueryIn req) {
        log.info("专题回收站分页查询家口开始：");
        //创建响应
        PageResponse resp = new PageResponse();
        req.setOrganizationId(SecurityUtils.getLoginUser().getSysUser().getOrganizationId());
        //分页查询
        Page<SysUser> page = PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<SpecialRecyclingListQueryOut> specialList = this.baseMapper.getSpecialRecyclingList(req);
        if (CollectionUtils.isNotEmpty(specialList)) {
            specialList.forEach(e -> {
                e.setColorName(Container.COLOR_TYPE.get(e.getColorType()));
                e.setColorNameEn(Container.COLOR_TYPE_EN.get(e.getColorType()));
            });
        }
        resp.setTotal(page.getTotal());
        resp.setList(specialList);
        resp.setPages(page.getPages());
        return resp;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R recoverSpecial(SpecialRecyclingRecoverIn req) {
        log.info("专题回收站恢复专题开始：req={}", req);
        //恢复专题
        SpecialRecycling specialRecycling = this.baseMapper.selectById(req.getRecyclingId());
        if (CommonConstant.NUMBER_0.equals(req.getOpcode())) {
            Special special1 = specialMapper.selectById(specialRecycling.getSpecialId());
            //校验专题编号
            LambdaQueryWrapper<Special> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Special::getTopicId, special1.getTopicId());
            wrapper.eq(Special::getDelFlag, CommonConstant.NUMBER_0);
            int count = specialMapper.selectCount(wrapper);
            if (count > 0) {
                return R.fail("专题编号已存在，禁止恢复");
            }
            //校验专题名称
            LambdaQueryWrapper<Special> qw = new LambdaQueryWrapper<>();
            qw.eq(Special::getSpecialName, special1.getSpecialName());
            qw.eq(Special::getDelFlag, CommonConstant.NUMBER_0);
            qw.eq(Special::getOrganizationId, SecurityUtils.getLoginUser().getSysUser().getOrganizationId());
            count = specialMapper.selectCount(qw);
            if (count > 0) {
                return R.fail("专题名称已存在，禁止恢复");
            }
            Special special = new Special();
            special.setSpecialId(specialRecycling.getSpecialId());
            special.setDelFlag(CommonConstant.NUMBER_0);
            specialMapper.updateById(special);
        }

        SpecialRecycling specialRecycling1 = new SpecialRecycling();
        specialRecycling1.setRecyclingId(specialRecycling.getRecyclingId());
        specialRecycling1.setDelFlag(CommonConstant.NUMBER_1);
        this.baseMapper.updateById(specialRecycling1);

        return R.ok();
    }
}
