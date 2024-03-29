package cn.staitech.fr.service.impl;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.core.utils.bean.BeanUtils;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.constant.Container;
import cn.staitech.fr.domain.Special;
import cn.staitech.fr.domain.SpecialRecycling;
import cn.staitech.fr.domain.in.EditSpecialStatusIn;
import cn.staitech.fr.domain.in.SpecialAddIn;
import cn.staitech.fr.domain.in.SpecialEditIn;
import cn.staitech.fr.domain.in.SpecialListQueryIn;
import cn.staitech.fr.domain.out.SpecialListQueryOut;
import cn.staitech.fr.domain.out.WaxBlockNumberListOut;
import cn.staitech.fr.mapper.SpecialMapper;
import cn.staitech.fr.service.SpecialRecyclingService;
import cn.staitech.fr.service.SpecialService;
import cn.staitech.system.api.domain.SysUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 专题表 服务实现类
 * </p>
 *
 * @author author
 * @since 2024-03-29
 */
@Service
@Slf4j
public class SpecialServiceImpl extends ServiceImpl<SpecialMapper, Special> implements SpecialService {
    @Autowired
    private SpecialRecyclingService specialRecyclingService;

    @Override
    public PageResponse<SpecialListQueryOut> getSpecialList(SpecialListQueryIn req) {
        log.info("专题列表查询接口开始：");
        //创建响应
        PageResponse resp = new PageResponse();
        //分页查询
        Page<SysUser> page = PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<SpecialListQueryOut> specialList = this.baseMapper.getSpecialList(req);
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
    public R addSpecial(SpecialAddIn req) {
        log.info("添加专题接口开始：");
        //校验专题编号唯一性
        LambdaQueryWrapper<Special> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Special::getOrganizationId, req.getOrganizationId());
        wrapper.eq(Special::getDelFlag, CommonConstant.NUMBER_0);
        wrapper.eq(Special::getTopicId, req.getTopicId());
        List<Special> specials = this.baseMapper.selectList(wrapper);
        if (CollectionUtils.isNotEmpty(specials)) {
            return R.fail("专题编号已存在，请重新输入！");
        }
        LambdaQueryWrapper<Special> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(Special::getOrganizationId, req.getOrganizationId());
        wrapper2.eq(Special::getDelFlag, CommonConstant.NUMBER_0);
        wrapper2.eq(Special::getSpecialName, req.getSpecialName());
        List<Special> specials2 = this.baseMapper.selectList(wrapper2);
        if (CollectionUtils.isNotEmpty(specials2)) {
            return R.fail("专题名称已存在，请重新输入！");
        }
        Special special = new Special();
        BeanUtils.copyProperties(req, special);
        special.setCreateBy(SecurityUtils.getUserId());
        special.setCreateTime(new Date());
        this.baseMapper.insert(special);
        return R.ok();
    }

    @Override
    public R editSpecial(SpecialEditIn req) {
        log.info("专题编辑接口开始：");
        LambdaQueryWrapper<Special> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Special::getOrganizationId, req.getOrganizationId());
        wrapper.eq(Special::getDelFlag, CommonConstant.NUMBER_0);
        wrapper.eq(Special::getSpecialName, req.getSpecialName());
        wrapper.ne(Special::getSpecialId, req.getSpecialId());
        List<Special> specials2 = this.baseMapper.selectList(wrapper);
        if (CollectionUtils.isNotEmpty(specials2)) {
            return R.fail("专题名称已存在，请重新输入！");
        }
        Special special = new Special();
        BeanUtils.copyProperties(req, special);
        special.setUpdateBy(SecurityUtils.getUserId());
        special.setUpdateTime(new Date());
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class )
    public R removeSpecial(Long specialId) {
        log.info("专题删除接口开始：specialId={}", specialId);
        Special special = this.baseMapper.selectById(specialId);
        if (special == null) {
            return R.fail("专题不存在，请刷新后重试！");
        }
        special.setDelFlag(CommonConstant.NUMBER_1);
        special.setUpdateBy(SecurityUtils.getUserId());
        special.setUpdateTime(new Date());
        this.baseMapper.updateById(special);
        //放入回收站
        SpecialRecycling specialRecycling = new SpecialRecycling();
        specialRecycling.setSpecialId(specialId);
        specialRecycling.setExpireTime(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000 * 30L));
        specialRecycling.setDelFlag(CommonConstant.NUMBER_1);
        specialRecycling.setCreateBy(SecurityUtils.getUserId());
        specialRecycling.setCreateTime(new Date());
        specialRecyclingService.save(specialRecycling);
        return R.ok();
    }

    @Override
    public R editSpecialStatus(EditSpecialStatusIn req) {
        log.info("专题状态按钮接口开始：");
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        Special special = new Special();
        special.setSpecialId(req.getSpecialId());
        special.setUpdateTime(new Date());
        special.setUpdateBy(sysUser.getUserId());
        special.setStatus(req.getStatus());
        this.baseMapper.updateById(special);
        return R.ok();
    }

}
