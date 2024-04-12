package cn.staitech.fr.service.impl;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.core.utils.bean.BeanUtils;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.constant.Container;
import cn.staitech.fr.domain.Group;
import cn.staitech.fr.domain.Image;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.domain.Special;
import cn.staitech.fr.domain.SpecialMember;
import cn.staitech.fr.domain.SpecialRecycling;
import cn.staitech.fr.domain.in.EditSpecialStatusIn;
import cn.staitech.fr.domain.in.SpecialAddIn;
import cn.staitech.fr.domain.in.SpecialEditIn;
import cn.staitech.fr.domain.in.SpecialListQueryIn;
import cn.staitech.fr.domain.in.SpecialsQueryIn;
import cn.staitech.fr.domain.out.SpecialListQueryOut;
import cn.staitech.fr.mapper.ImageMapper;
import cn.staitech.fr.mapper.SlideMapper;
import cn.staitech.fr.mapper.SpecialMapper;
import cn.staitech.fr.mapper.SpecialMemberMapper;
import cn.staitech.fr.mapper.WaxBlockInfoMapper;
import cn.staitech.fr.service.GroupService;
import cn.staitech.fr.service.SlideService;
import cn.staitech.fr.service.SpecialRecyclingService;
import cn.staitech.fr.service.SpecialService;
import cn.staitech.fr.utils.MessageSource;
import cn.staitech.system.api.domain.SysUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
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

    @Resource
    private ImageMapper imageMapper;

    @Resource
    private SlideMapper slideMapper;

    @Autowired
    private SlideService slideService;

    @Autowired
    private GroupService groupService;

    @Resource
    private WaxBlockInfoMapper waxBlockInfoMapper;

    @Resource
    private SpecialMemberMapper specialMemberMapper;

    @Override
    public PageResponse<SpecialListQueryOut> getSpecialList(SpecialListQueryIn req) {
        log.info("专题列表查询接口开始：");
        //创建响应
        PageResponse resp = new PageResponse();
        //分页查询
        req.setOrganizationId(SecurityUtils.getLoginUser().getSysUser().getOrganizationId());

        Page<SysUser> page = PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<SpecialListQueryOut> specialList = this.baseMapper.getSpecialList(req);
        if (CollectionUtils.isNotEmpty(specialList)) {
            specialList.forEach(e -> {

                e.setColorName(Container.COLOR_TYPE.get(Integer.valueOf(e.getColorType())));
                e.setColorNameEn(Container.COLOR_TYPE_EN.get(Integer.valueOf(e.getColorType())));
                e.setTrialType(Container.TRIAL_TYPE.get(e.getTrialId()));
                e.setTrialTypeEn(Container.TRIAL_TYPE_EN.get(e.getTrialId()));
            });
        }
        resp.setTotal(page.getTotal());
        resp.setList(specialList);
        resp.setPages(page.getPages());
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        //初始化切片
        LambdaQueryWrapper<Image> qw = new LambdaQueryWrapper<>();
        qw.eq(Image::getOrganizationId, req.getOrganizationId());
        qw.eq(Image::getStatus, CommonConstant.NUMBER_4);
        qw.eq(Image::getTopicId, req.getTopicId());
        List<Image> images = imageMapper.selectList(qw);
        List<Slide> arrayList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(images)) {
            for (Image image : images) {
                Slide slide = new Slide();
                slide.setCreateBy(SecurityUtils.getUserId());
                slide.setCreateTime(new Date());
                slide.setImageId(image.getImageId());
                slide.setSpecialId(special.getSpecialId());
                getExtInfo(image.getFileName(), slide, special.getSpecialId(), req);
                arrayList.add(slide);
            }
        }
        slideService.saveBatch(arrayList);
        //专题成员
        SpecialMember specialMember = new SpecialMember();
        specialMember.setSpecialId(special.getSpecialId());
        specialMember.setUserId(SecurityUtils.getUserId());
        specialMember.setOrganizationId(SecurityUtils.getLoginUser().getSysUser().getOrganizationId());
        specialMember.setCreateBy(SecurityUtils.getUserId());
        specialMember.setCreateTime(new Date());
        specialMemberMapper.insert(specialMember);
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
        this.baseMapper.updateById(special);
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        int slideNum=getSlideNum(specialId);
        specialRecycling.setSlideNum(slideNum);
        specialRecycling.setDelFlag(CommonConstant.NUMBER_0);
        specialRecycling.setCreateBy(SecurityUtils.getUserId());
        specialRecycling.setCreateTime(new Date());
        specialRecyclingService.save(specialRecycling);
        //删除切片
        Slide slide = new Slide();
        slide.setDelFlag(CommonConstant.NUMBER_1);
        LambdaQueryWrapper<Slide> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Slide::getDelFlag, CommonConstant.NUMBER_0);
        wrapper.eq(Slide::getSpecialId, specialId);
        slideService.update(slide, wrapper);
        return R.ok();
    }

    private int getSlideNum(Long specialId) {
        LambdaQueryWrapper<Slide> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Slide::getDelFlag, CommonConstant.NUMBER_0);
        wrapper.eq(Slide::getSpecialId, specialId);
        return slideService.count(wrapper);
    }

    @Override
    public R editSpecialStatus(EditSpecialStatusIn req) {
        log.info("专题状态按钮接口开始：");
        //启动条件判断
        if (req.getStatus().equals(CommonConstant.INT_1)) {
            LambdaQueryWrapper<Slide> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Slide::getSpecialId, req.getSpecialId());
            wrapper.ne(Slide::getCheckStatus, 1);
            List<Slide> slideList = slideService.list(wrapper);
            if (CollectionUtils.isNotEmpty(slideList)) {
                return R.fail(MessageSource.M("START_SPECIAL_ERROR"));
            }
        }

        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        Special special = new Special();
        special.setSpecialId(req.getSpecialId());
        special.setUpdateTime(new Date());
        special.setUpdateBy(sysUser.getUserId());
        special.setStatus(req.getStatus());
        this.baseMapper.updateById(special);
        return R.ok();
    }

    @Override
    public PageResponse<SpecialListQueryOut> getSpecials(SpecialsQueryIn req) {
        log.info("智能阅片专题列表接口开始：");
        //创建响应
        PageResponse resp = new PageResponse();
        //分页查询
        req.setOrganizationId(SecurityUtils.getLoginUser().getSysUser().getOrganizationId());
        req.setLoginUserId(SecurityUtils.getUserId());
        Page<SysUser> page = PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<SpecialListQueryOut> specialList = this.baseMapper.getSpecials(req);
        if (CollectionUtils.isNotEmpty(specialList)) {
            specialList.forEach(e -> {
                e.setColorName(Container.COLOR_TYPE.get(Integer.valueOf(e.getColorType())));
                e.setColorNameEn(Container.COLOR_TYPE_EN.get(Integer.valueOf(e.getColorType())));
                e.setTrialType(Container.TRIAL_TYPE.get(e.getTrialId()));
                e.setTrialTypeEn(Container.TRIAL_TYPE_EN.get(e.getTrialId()));
            });
        }
        resp.setTotal(page.getTotal());
        resp.setList(specialList);
        resp.setPages(page.getPages());
        return resp;
    }

    private Slide getExtInfo(String fileName, Slide slide, Long specialId, SpecialAddIn req) {
        String[] s = fileName.split(" ");
        if (s.length != 3) {
            log.info("切片文件名格式错误：" + fileName);
            return slide;
        }
        String s1 = slideMapper.selectBySpecialId(specialId);
        if (!s[0].equals(s1)) {
            log.info("切片文件名格式错误：" + fileName);
            return slide;
        }
        slide.setAnimalCode(StringUtils.substringBeforeLast(s[1], "-"));
        slide.setWaxCode(StringUtils.substringAfterLast(s[1], "-"));
        //判断性别数据
        if (!CommonConstant.MALE.equals(s[2].substring(s[2].length() - 1)) &&
                !CommonConstant.FEMALE.equals(s[2].substring(s[2].length() - 1))) {
            log.info("切片文件名格式错误：" + fileName);
            return slide;
        }
        slide.setGenderFlag(s[2].substring(s[2].length() - 1));
        //判断组别
        /*Group byId = groupService.getById(s[2].substring(0, s[2].length() - 1));
        if (ObjectUtils.isEmpty(byId)) {
            log.info("切片文件名格式错误：" + fileName);
            return slide;
        }*/
        slide.setGroupCode(s[2].substring(0, s[2].length() - 1));

        slide.setOrgans(waxBlockInfoMapper.getOrganName(req.getTopicId(), req.getSpeciesId(), slide.getWaxCode(),s[2].substring(s[2].length() - 1)));

        return slide;
    }
}
