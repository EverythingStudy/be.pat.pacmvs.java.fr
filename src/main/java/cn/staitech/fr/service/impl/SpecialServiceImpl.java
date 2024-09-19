package cn.staitech.fr.service.impl;

import static cn.staitech.common.core.constant.UserConstants.RSA_KEYS;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import cn.staitech.common.core.constant.SecurityConstants;
import cn.staitech.common.core.constant.UserConstants;
import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.core.enums.UserStatus;
import cn.staitech.common.core.exception.ServiceException;
import cn.staitech.common.core.utils.RSAUtils;
import cn.staitech.common.core.utils.bean.BeanUtils;
import cn.staitech.common.redis.service.RedisService;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.constant.Container;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.Image;
import cn.staitech.fr.domain.PathologicalIndicator;
import cn.staitech.fr.domain.PathologicalIndicatorCategory;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.domain.Special;
import cn.staitech.fr.domain.SpecialAnnotationRel;
import cn.staitech.fr.domain.SpecialLockLog;
import cn.staitech.fr.domain.SpecialMember;
import cn.staitech.fr.domain.SpecialRecycling;
import cn.staitech.fr.domain.in.EditSpecialStatusIn;
import cn.staitech.fr.domain.in.SpecialAddIn;
import cn.staitech.fr.domain.in.SpecialEditIn;
import cn.staitech.fr.domain.in.SpecialListQueryIn;
import cn.staitech.fr.domain.in.SpecialsQueryIn;
import cn.staitech.fr.domain.out.SpecialListQueryOut;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.ImageMapper;
import cn.staitech.fr.mapper.PathologicalIndicatorCategoryMapper;
import cn.staitech.fr.mapper.PathologicalIndicatorMapper;
import cn.staitech.fr.mapper.SlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.mapper.SpecialLockLogMapper;
import cn.staitech.fr.mapper.SpecialMapper;
import cn.staitech.fr.mapper.SpecialMemberMapper;
import cn.staitech.fr.mapper.TopicMapper;
import cn.staitech.fr.service.SlideService;
import cn.staitech.fr.service.SpecialRecyclingService;
import cn.staitech.fr.service.SpecialService;
import cn.staitech.fr.utils.MessageSource;
import cn.staitech.system.api.RemoteUserService;
import cn.staitech.system.api.domain.SysUser;
import cn.staitech.system.api.model.LoginUser;
import lombok.extern.slf4j.Slf4j;

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


    @Resource
    private SpecialMemberMapper specialMemberMapper;

    @Resource
    private TopicMapper topicMapper;

    @Resource
    private RedisService redisService;

    @Autowired
    private RemoteUserService remoteUserService;

    @Resource
    private SpecialLockLogMapper specialLockLogMapper;

    @Resource
    private SpecialAnnotationRelMapper specialAnnotationRelMapper;

    @Resource
    private AnnotationMapper annotationMapper;
    
    @Resource
    private SpecialMapper specialMapper;

    @Resource
    private PathologicalIndicatorMapper pathologicalIndicatorMapper;

    @Resource
    private PathologicalIndicatorCategoryMapper pathologicalIndicatorCategoryMapper;



    @Override
    public List<PathologicalIndicatorCategory> speciesCategory(Long specialId){
        Special special = specialMapper.selectById(specialId);
        LambdaQueryWrapper<PathologicalIndicator> indicatorQueryWrapper = new LambdaQueryWrapper<>();
        indicatorQueryWrapper.eq(PathologicalIndicator::getDelFlag,0).eq(PathologicalIndicator::getSpeciesId, special.getSpeciesId()).eq(PathologicalIndicator::getOrganizationId, special.getOrganizationId());
        List<PathologicalIndicator> indicatorList = pathologicalIndicatorMapper.selectList(indicatorQueryWrapper);
        if(indicatorList.size() > 0){
            // 获取指标id
            List<Long> indicatorIdList = indicatorList.stream().map(PathologicalIndicator::getIndicatorId).collect(Collectors.toList());
            LambdaQueryWrapper<PathologicalIndicatorCategory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(PathologicalIndicatorCategory::getIndicatorId, indicatorIdList).eq(PathologicalIndicatorCategory::getDelFlag,0);
            return pathologicalIndicatorCategoryMapper.selectList(queryWrapper);
        }
        return new ArrayList<>();
    }


    @Override
    public PageResponse<SpecialListQueryOut> getSpecialList(SpecialListQueryIn req) {
        log.info("专题列表查询接口开始：");
        //创建响应
        PageResponse resp = new PageResponse();
        //分页查询
        req.setOrganizationId(SecurityUtils.getLoginUser().getSysUser().getOrganizationId());
        //判断是不是管理员
        Integer integer = this.baseMapper.countgetUserRole(SecurityUtils.getUserId());
        if (integer == 0) {
            req.setUserId(SecurityUtils.getUserId());
        }
        Page<SpecialListQueryOut> page = PageHelper.startPage(req.getPageNum(), req.getPageSize());
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
//    @Transactional(rollbackFor = Exception.class)
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
        qw.eq(Image::getAnalyzeStatus, CommonConstant.INT_1);
        List<Image> images = imageMapper.selectList(qw);
        List<Slide> arrayList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(images)) {
            for (Image image : images) {
                Slide slide = new Slide();
                slide.setCreateBy(SecurityUtils.getUserId());
                slide.setCreateTime(new Date());
                slide.setImageId(image.getImageId());
                slide.setSpecialId(special.getSpecialId());
                //getExtInfo(image.getFileName(), slide, special.getSpecialId(), req);
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
        // 创建专题轮廓关系
        getSpecialAnnotationRel(special.getSpecialId(), SecurityUtils.getUserId());
        return R.ok();
    }


    public SpecialAnnotationRel getSpecialAnnotationRel(Long specialId, Long userId) {
        //缓存获取
        SpecialAnnotationRel cacheSpecialAnnotationRel = redisService.getCacheObject(CommonConstant.SPECIAL_ANNOTATION_REL + specialId);
        if (null == cacheSpecialAnnotationRel) {
            QueryWrapper<SpecialAnnotationRel> parQueryWrapperBy = new QueryWrapper<>();
            parQueryWrapperBy.eq("special_id", specialId);
            List<SpecialAnnotationRel> parList = specialAnnotationRelMapper.selectList(parQueryWrapperBy);
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(parList)) {
                redisService.setCacheObject(CommonConstant.SPECIAL_ANNOTATION_REL + specialId, parList.get(0), CommonConstant.SPECIAL_ANNOTATION_REL_CACHE_DAYS, TimeUnit.DAYS);
                return parList.get(0);
            } else {
                cacheSpecialAnnotationRel = new SpecialAnnotationRel();
                //查下可以使用的表
                QueryWrapper<SpecialAnnotationRel> userQueryWrapperBy = new QueryWrapper<>();
                userQueryWrapperBy.orderByDesc("sequence_number");
                userQueryWrapperBy.last("LIMIT 1");
                List<SpecialAnnotationRel> userPARList = specialAnnotationRelMapper.selectList(userQueryWrapperBy);
                if (org.apache.commons.collections4.CollectionUtils.isEmpty(userPARList)) {
                    //新建表-从1开始
                    synchronized (this) {
                        Long sequenceNumber = 1L;
                        cacheSpecialAnnotationRel = generateTable(cacheSpecialAnnotationRel, specialId, userId, sequenceNumber);
                        return cacheSpecialAnnotationRel;
                    }
                } else {
                    SpecialAnnotationRel pAnnoRel = userPARList.get(0);
                    Long currentSequenceNumber = pAnnoRel.getSequenceNumber();
                    if(null == currentSequenceNumber) {
                    	synchronized (this) {
                            Long sequenceNumber = 1L;
                            cacheSpecialAnnotationRel = generateTable(cacheSpecialAnnotationRel, specialId, userId, sequenceNumber);
                            return cacheSpecialAnnotationRel;
                        }
                    }
                    //查下当前表的项目个数和记录条数，如果可以用继续，如果不可以就新建表
                    Annotation queryAnnotation = new Annotation();
                    queryAnnotation.setSequenceNumber(currentSequenceNumber);
                    
                    Integer totalProjects = specialAnnotationRelMapper.selectTableSpecialCount(queryAnnotation);
                    Integer totalRecords = annotationMapper.selectTableRecordCount(queryAnnotation);
                    if (totalProjects >= CommonConstant.PROJECT_NUMBER_LIMIT || totalRecords >= CommonConstant.TABLE_RECORD_LIMIT) {
                        //新建表
                        Long sequenceNumber = currentSequenceNumber + 1;
                        synchronized (this) {
                            cacheSpecialAnnotationRel = generateTable(cacheSpecialAnnotationRel, specialId, userId, sequenceNumber);
                            return cacheSpecialAnnotationRel;
                        }
                    } else {
                        //3、insert 记录
                        cacheSpecialAnnotationRel.setSpecialId(specialId);
                        cacheSpecialAnnotationRel.setSequenceNumber(currentSequenceNumber);
                        cacheSpecialAnnotationRel.setCreateBy(userId);
                        cacheSpecialAnnotationRel.setCreateTime(new Date());
                        specialAnnotationRelMapper.insert(cacheSpecialAnnotationRel);
                        redisService.setCacheObject(CommonConstant.SPECIAL_ANNOTATION_REL + specialId, cacheSpecialAnnotationRel, CommonConstant.SPECIAL_ANNOTATION_REL_CACHE_DAYS, TimeUnit.DAYS);
                        return cacheSpecialAnnotationRel;
                    }
                }
            }
        } else {
            //已有表继续使用
            return cacheSpecialAnnotationRel;
        }
    }

    private SpecialAnnotationRel generateTable(SpecialAnnotationRel cacheSpecialAnnotationRel, Long specialId, Long userId, Long sequenceNumber) {
        Annotation annotation = new Annotation();
        annotation.setSequenceNumber(sequenceNumber);
        //先确认是否存在这个表了，如果存在就不新建表了
        Integer existTable = annotationMapper.selectExistTable(annotation);
        if (existTable == 0) {
            //1、Sequence
        	annotationMapper.createTableSequence(annotation);
            //2、建表
        	annotationMapper.createTable(annotation);
        }
        //3、insert 记录
        cacheSpecialAnnotationRel.setSpecialId(specialId);
        cacheSpecialAnnotationRel.setSequenceNumber(sequenceNumber);
        cacheSpecialAnnotationRel.setCreateBy(userId);
        cacheSpecialAnnotationRel.setCreateTime(new Date());
        specialAnnotationRelMapper.insert(cacheSpecialAnnotationRel);
        redisService.setCacheObject(CommonConstant.SPECIAL_ANNOTATION_REL + specialId, cacheSpecialAnnotationRel, CommonConstant.SPECIAL_ANNOTATION_REL_CACHE_DAYS, TimeUnit.DAYS);
        return cacheSpecialAnnotationRel;
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
    		return R.fail(MessageSource.M("EXISTS_SPECIAL_DATA"));
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
            return R.fail(MessageSource.M("DATA_DOES_NOT_EXIST"));
        }
        special.setDelFlag(CommonConstant.NUMBER_1);
        special.setUpdateBy(SecurityUtils.getUserId());
        special.setUpdateTime(new Date());
        this.baseMapper.updateById(special);
        //放入回收站
        SpecialRecycling specialRecycling = new SpecialRecycling();
        specialRecycling.setSpecialId(specialId);
        specialRecycling.setExpireTime(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000 * 30L));
        int slideNum = getSlideNum(specialId);
        specialRecycling.setSlideNum(slideNum);
        specialRecycling.setDelFlag(CommonConstant.NUMBER_0);
        specialRecycling.setCreateBy(SecurityUtils.getUserId());
        specialRecycling.setCreateTime(new Date());
        specialRecyclingService.save(specialRecycling);
        //删除切片
        /*Slide slide = new Slide();
        slide.setDelFlag(CommonConstant.NUMBER_1);
        LambdaQueryWrapper<Slide> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Slide::getDelFlag, CommonConstant.NUMBER_0);
        wrapper.eq(Slide::getSpecialId, specialId);
        slideService.update(slide, wrapper);*/
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
    	Special special = this.baseMapper.selectById(req.getSpecialId());
    	//启动条件判断
    	if (req.getStatus().equals(CommonConstant.INT_1)) {
    		//启动和取消完成传入的状态都是1，如何判断是取消完整呢，需要看之前的状态是否是“完成状态”，如果是完成责是取消完成 反之是启动
    		//状态(0待启动，1进行中，2暂停，3已完成，4锁定)
    		int status = special.getStatus();
    		if(status == 0) {
    			//判断专题下是否存在切片数据
    			LambdaQueryWrapper<Slide> queryWrapper = new LambdaQueryWrapper<>();
    			queryWrapper.eq(Slide::getSpecialId, req.getSpecialId());
    			queryWrapper.eq(Slide::getDelFlag, CommonConstant.NUMBER_0);
    			List<Slide> slides = slideService.list(queryWrapper);
    			if (CollectionUtils.isEmpty(slides)) {
    				return R.fail(MessageSource.M("NO_SLIDE_DATA_CANNOT_START"));
    			}
    		}
    	}

    	//锁定传4,解锁 5
    	if (req.getStatus().equals(CommonConstant.INT_4) || req.getStatus().equals(CommonConstant.INT_5)) {
    		
    		if (special == null) {
    			return R.fail(MessageSource.M("DATA_DOES_NOT_EXIST"));
    		}
    		//状态(0待启动，1进行中，2暂停，3已完成，4锁定)
    		Integer status = special.getStatus();
    		//已经锁定判断
    		if(status.equals(CommonConstant.INT_4) && req.getStatus().equals(CommonConstant.INT_4)){
    			return R.fail(MessageSource.M("SPECIAL_HAVE_LOCK"));
    		}
    		//解锁判断
    		if(status.equals(CommonConstant.INT_1) && req.getStatus().equals(CommonConstant.INT_5)){
    			return R.fail(MessageSource.M("SPECIAL_HAVE_UNLOCK"));
    		}
    	}

    	//锁定传4,解锁 5 校验
    	if (req.getStatus().equals(CommonConstant.INT_4) || req.getStatus().equals(CommonConstant.INT_5)) {
    		//校验用户名、密码
    		boolean res = userLoginVerify(req.getUserName(), req.getPwd(),req);
    		if (!res) {
    			return R.fail("校验失败");
    		}

    		if (req.getStatus().equals(CommonConstant.INT_5)) {
    			//解锁 赋值为1
    			req.setStatus(1);
    		}
    	}
    	SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
    	Long currentUserId = sysUser.getUserId();
    	Long specialId = req.getSpecialId();

    	Special bean = new Special();
    	bean.setSpecialId(specialId);
    	bean.setUpdateTime(new Date());
    	bean.setUpdateBy(currentUserId);
    	bean.setStatus(req.getStatus());
    	this.baseMapper.updateById(bean);
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

    @Override
    public R<Special> getInfoById(Long specialId) {
        log.info("智能阅片专题详情接口开始：");
        Special special = this.baseMapper.selectById(specialId);
        special.setTopicName(topicMapper.selectById(special.getTopicId()).getTopicName());
        return R.ok(special);
    }



    public Boolean userLoginVerify(String username, String pwd,EditSpecialStatusIn req) {
        // 获取密钥对解密密码
        String cacheObject = redisService.getCacheObject(RSA_KEYS + username);
        if (Objects.isNull(cacheObject)) {
            throw new ServiceException("当前用户名和密码错误，请核对");
        }
        String password;
        //解析
        try {
            password = RSAUtils.getStringByPrivateKey(cacheObject, pwd);
        } catch (Exception e) {
            throw new ServiceException("密码解密异常" + e);
        }
        //每次解析完删除密钥对
        redisService.deleteObject(RSA_KEYS + username);
        //
        //log.info("解密后的密码" + password);
        // 用户名或密码为空 错误
        if (StringUtils.isAnyBlank(username, password)) {
            throw new ServiceException("用户/密码必须填写");
        }
        // 密码如果不在指定范围内 错误
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            throw new ServiceException("用户密码不在指定范围");
        }
        // 用户名不在指定范围内 错误
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            throw new ServiceException("用户名不在指定范围");
        }
        // 查询用户信息
        R<LoginUser> userResult = remoteUserService.getUserInfo(username, SecurityConstants.INNER);
        //远程接口调用异常
        if (R.FAIL == userResult.getCode()) {
            throw new ServiceException(userResult.getMsg());
        }
        //用户数据为空
        if (cn.staitech.common.core.utils.StringUtils.isNull(userResult) || cn.staitech.common.core.utils.StringUtils.isNull(userResult.getData())) {
            throw new ServiceException("登录用户：" + username + " 不存在");
        }

        SysUser user = userResult.getData().getSysUser();
        if (UserStatus.DISABLE.getCode().equals(user.getDelFlag())) {
            throw new ServiceException("对不起，您的账号：" + username + " 已被删除");
        }
        if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            throw new ServiceException("对不起，您的账号：" + username + " 已停用");
        }
        if (!SecurityUtils.matchesPassword(password, user.getPassword())) {
            throw new ServiceException("用户不存在/密码错误");
        }
        
      //锁定传4,解锁 5 增加日志
    	SpecialLockLog entity = new SpecialLockLog();
    	entity.setSpecialId(req.getSpecialId());
    	if (req.getStatus().equals(CommonConstant.INT_4)) {
    		entity.setType(CommonConstant.INT_4);
    	} else {
    		entity.setType(CommonConstant.INT_5);
    	}
    	entity.setCreateBy(user.getUserId());
    	entity.setCreateTime(new Date());
    	entity.setReason(req.getReason());
    	specialLockLogMapper.insert(entity);
        return true;
    }

	@Override
	public SysUser getUserInfo(Map<String, Object> parm) {
		String cacheKey = "user_";
		if(null!=parm && parm.containsKey("userId")){
			Long cacheUserIdKey = (Long) parm.get("userId");
			cacheKey = cacheKey+cacheUserIdKey.toString(); 
		}

		if(null!=parm && parm.containsKey("userName")){
			String userName =(String) parm.get("userName");
			Long organizationId =(Long) parm.get("organizationId");
			cacheKey = cacheKey+organizationId+"_"+userName;
		}

		SysUser sysUser = redisService.getCacheObject(cacheKey);
		if(null != sysUser){
			return sysUser;
		}else{
			List<SysUser> userList = specialMapper.selectUserById(parm);
			if(CollectionUtils.isNotEmpty(userList)){
				sysUser = userList.get(0);
				redisService.setCacheObject(cacheKey,sysUser, 31L, TimeUnit.DAYS);
			}
		}
		return sysUser;
	}

}
