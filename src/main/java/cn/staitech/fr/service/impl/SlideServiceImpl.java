package cn.staitech.fr.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import cn.staitech.common.core.domain.CustomPage;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.Constants;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.service.SlideService;
import cn.staitech.fr.vo.project.*;
import cn.staitech.fr.vo.project.slide.*;
import cn.staitech.system.api.RemoteAnnotationService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.staitech.common.security.utils.SecurityUtils.isAdmin;

/**
 * @author mugw
 * @version 2.6.0
 * @description 项目切片管理
 * @date 2025/5/14 13:44:14
 */
@Service
@Slf4j
public class SlideServiceImpl extends ServiceImpl<SlideMapper, Slide> implements SlideService {

	@Resource
	private ProjectMapper projectMapper;
	@Resource
	private ProjectMemberMapper projectMemberMapper;
	@Resource
	private ImageMapper imageMapper;
	@Resource
	private RemoteAnnotationService remoteAnnotationService;
	@Resource
	private RedisTemplate<String,Object> redisTemplate;
	@Resource
	private ProductionMapper productionMapper;
	@Value("${ai.url:http://192.168.160.112:8003/CreateAIwtr}")
	private String aiUrl;

	@Override
	public R<CustomPage<SlidePageVo>> page(SlidePageReq req, boolean isPageConfigSlide, boolean isAccessPermission) {
		log.info("项目下切片列表查询接口开始，请求参数: {}", req);

		Long userId = SecurityUtils.getUserId();
		Project project = projectMapper.selectById(req.getProjectId());

		if (project == null) {
			log.warn("项目不存在，projectId: {}", req.getProjectId());
			return R.fail("您没有该项目的访问权限，请联系该项目负责人或机构管理员");
		}

		if (isAccessPermission){
			// 检查用户是否为成员
			long isMember = projectMemberMapper.selectCount(Wrappers.<ProjectMember>lambdaQuery()
					.eq(ProjectMember::getProjectId, req.getProjectId())
					.eq(ProjectMember::getUserId, userId)
					.eq(ProjectMember::getDelFlag, cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL));

			// 权限校验
			if (!hasAccessPermission(project, userId, isMember)) {
				log.warn("用户无访问权限，userId: {}, projectId: {}", userId, req.getProjectId());
				return R.fail("您没有该项目的访问权限，请联系该项目负责人或机构管理员");
			}
		}

		// 检查项目状态
		Integer status = project.getStatus();
		if (!isPageConfigSlide) {
			if (status != Constants.STATUS_RUNNING) {
				log.warn("项目状态不可访问，projectId: {}, status: {}", req.getProjectId(), status);
				return R.fail("非进行中的项目不可阅片，请联系该项目负责人或机构管理员");
			}
		}
		// 分页查询
		CustomPage<SlidePageVo> page = new CustomPage<>(req);
		req.setCurrentUserId("JSON_CONTAINS(fs.viewers, '"+userId+"', '$')");
		baseMapper.page(page, req);
		log.info("项目下切片列表查询接口结束");
		return R.ok(page);
	}

	/**
	 * 判断用户是否有访问权限
	 */
	private boolean hasAccessPermission(Project project, Long userId, long isMember) {
		return project.getOrganizationId() == SecurityUtils.getOrganizationId()
				|| userId == project.getPrincipal()
				|| SecurityUtils.isOrgAdmin()
				|| isMember > 0;
	}

	@Override
	public R<CustomPage<ImageVO>> choiceImageList(ChoiceImagePageReq image) {
		Objects.requireNonNull(image, "请求参数不能为空");

		R validationResult = validateProjectStatus(image.getProjectId());
		if (validationResult != null) {
			return validationResult;
		}

		if (!isAdmin(SecurityUtils.getUserId())) {
			image.setOrgId(SecurityUtils.getOrganizationId());
		}

		CustomPage<ImageVO> page = new CustomPage<>(image);
		imageMapper.choiceImageList(page, image);
		return R.ok(page);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R choiceSave(ProjectImageVo req) {
		Objects.requireNonNull(req, "请求参数不能为空");
		List<ImageVO> images = req.getImages();
		Objects.requireNonNull(images, "图片列表不能为空");

		log.info("切片选择保存接口开始，用户ID：{}，ProjectId：{}", SecurityUtils.getUserId(), req.getProjectId());

		R validationResult = validateProjectStatus(req.getProjectId());
		if (validationResult != null) {
			return validationResult;
		}

		List<Slide> slidesToSave = new ArrayList<>();
		Long userId = SecurityUtils.getUserId();
		List<Long> imageIds = imageIdsFilter(images.stream().map(ImageVO::getImageId).collect(Collectors.toList()), req.getProjectId());
		for (Long imageId : imageIds) {
			Slide slide = new Slide();
			slide.setCreateBy(userId);
			slide.setCreateTime(new Date());
			slide.setImageId(imageId);
			slide.setProjectId(req.getProjectId());
			slidesToSave.add(slide);
		}
		saveBatch(slidesToSave);
		return R.ok();
	}

	@Override
	public R choiceAll(Long projectId) throws Exception{

		R validationResult = validateProjectStatus(projectId);

		if (validationResult != null) {
			return validationResult;
		}
		Project project = projectMapper.selectById(projectId);
		Long topicId = project.getTopicId();
		List<Image> images = imageMapper.selectList(Wrappers.<Image>lambdaQuery().eq(Image::getTopicId, topicId)
				.eq(Image::getStatus, Constants.IMAGE_STATUS_ENABLE)
				.eq(Image::getAnalyzeStatus, Constants.IMAGE_NAME_PARSE_SUCC));
		if (CollectionUtils.isEmpty(images)){
			return R.fail("没有可关联的新切片");
		}
		List<Slide> slidesToSave = new ArrayList<>();
		Long userId = SecurityUtils.getUserId();
		List<Long> imageIds = imageIdsFilter(images.stream().map(Image::getImageId).collect(Collectors.toList()), projectId);
		for (Long imageId : imageIds) {
			Slide slide = new Slide();
			slide.setCreateBy(userId);
			slide.setCreateTime(new Date());
			slide.setImageId(imageId);
			slide.setProjectId(projectId);
			slidesToSave.add(slide);
		}
		saveBatch(slidesToSave);
		return R.ok();
	}

	private List<Long> imageIdsFilter(List<Long> imageIds,  Long projectId) {
		List<Slide> slides = list(Wrappers.<Slide>lambdaQuery().eq(Slide::getDelFlag, cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL)
				.eq(Slide::getProjectId, projectId)
				.in(Slide::getImageId, imageIds).select(Slide::getImageId));
		if (CollectionUtils.isNotEmpty(slides)) {
			List<Long> temp = slides.stream().map(Slide::getImageId).collect(Collectors.toList());
			imageIds.removeAll(temp);
		}
		return imageIds;
	}

	@Override
	public R deleteSlide(Long projectId, List<Long> slideIds) throws Exception{
		log.info("删除全部切片接口开始：");
		R validationResult = validateProjectStatus(projectId);
		if (validationResult != null) {
			return validationResult;
		}
		List<Slide> slideList = list(Wrappers.<Slide>lambdaQuery().eq(ObjectUtil.isNotEmpty(projectId),Slide::getProjectId,projectId)
				.in(CollectionUtils.isNotEmpty(slideIds),Slide::getSlideId,slideIds)
				.eq(Slide::getDelFlag, cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL).select(Slide::getSlideId));
		if(CollectionUtils.isNotEmpty(slideList)){
			slideList.forEach(slide -> {slide.setDelFlag(cn.staitech.common.core.constant.Constants.DEL_FLAG_DELETED);});
			updateBatchById(slideList);
			R<Boolean> deleteResult = remoteAnnotationService.deleteBySlide(slideList.stream().map(Slide::getSlideId).collect(Collectors.toList()));
		}
		return R.ok();
	}

	@Override
	public R checkDeleteSlide(Long projectId, List<Long> slideIds) throws  Exception{
		List<Slide> slideList = list(Wrappers.<Slide>lambdaQuery().eq(ObjectUtil.isNotEmpty(projectId),Slide::getProjectId,projectId)
				.in(CollectionUtils.isNotEmpty(slideIds),Slide::getSlideId,slideIds)
				.eq(Slide::getDelFlag, cn.staitech.common.core.constant.Constants.DEL_FLAG_NORMAL).select(Slide::getSlideId));
		if (CollectionUtils.isEmpty(slideList)){
			return R.fail("未找到要删除的切片");
		}
		R<Long> resp = remoteAnnotationService.countAnnoBySlides(slideList.stream().map(Slide::getSlideId).collect(Collectors.toList()));
		if (resp.getCode() == 500){
			return resp;
		}else{
			return R.ok(resp.getData()>0);
		}
	}

	/**
	 * 验证项目状态是否允许配置
	 *
	 * @param projectId 项目对象
	 * @return 如果状态不合法，返回错误 R.fail；否则继续执行
	 */
	private R validateProjectStatus(Long projectId) {
		Project project = projectMapper.selectById(projectId);
		Integer status = project.getStatus();

		if (status == Constants.STATUS_RUNNING) {
//			return R.fail("项目状态为“进行中”时，不能配置项目基础信息和切片");
			return R.fail("项目进行中，除配置用户外不可修改其他配置");
		}

		if (status == Constants.STATUS_COMPLETED) {
//			return R.fail("项目状态为“完成”时，任何用户不能再配置项目任何信息");
			return R.fail("项目已完成，不可修改配置");
		}

		if (status == Constants.STATUS_PAUSED
				&& !(SecurityUtils.getUserId() == project.getPrincipal() || SecurityUtils.isOrgAdmin())) {
//			return R.fail("项目状态为“暂停”时，机构管理员和项目负责人可以配置项目基础信息");
			return R.fail("您没有该项目的配置权限，请联系该项目负责人或机构管理员");
		}

		return null; // 表示通过校验
	}


	@Override
	public HashMap<String, SlidePageVo> slideAdjacent(SlidePageReq req) {
		req.setCurrentUserId("JSON_CONTAINS(fs.viewers, '"+SecurityUtils.getUserId()+"', '$')");
		List<SlidePageVo> waxList = baseMapper.slideListQuery(req);

		// 防止 waxList 为 null
		if (waxList == null) {
			waxList = Collections.emptyList();
		}

		int index = -1;
		Long targetSlideId = req.getSlideId();

		// 查找目标 slideId 所在位置
		for (int i = 0; i < waxList.size(); i++) {
			if (targetSlideId.equals(waxList.get(i).getSlideId())) {
				index = i;
				break;
			}
		}

		HashMap<String, SlidePageVo> map = new HashMap<>();
		map.put("prev", null);
		map.put("next", null);

		if (index != -1) {
			if (index > 0) {
				map.put("prev", waxList.get(index - 1));
			}
			if (index < waxList.size() - 1) {
				map.put("next", waxList.get(index + 1));
			}
		}

		return map;
	}

	@Override
	public SlideDetailVo getSlideInfo(Long slideId) {
		SlideDetailVo slideInfo = baseMapper.getSlideInfo(slideId);
		if (Objects.nonNull(slideInfo)){
			List<Long> viewers = slideInfo.getViewers();
			if (CollectionUtils.isNotEmpty(viewers) && !viewers.contains(SecurityUtils.getUserId())){
				viewers.add(SecurityUtils.getUserId());
			}
			if (CollectionUtils.isEmpty(viewers)){
				viewers = new ArrayList<>();
				viewers.add(SecurityUtils.getUserId());
			}
			update(Wrappers.<Slide>lambdaUpdate().eq(Slide::getSlideId,slideId).set(Slide::getViewers, viewers, "typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler"));
		}
		return slideInfo;
	}

	@Override
	public SlideSelectListVo getAnimalCode(SlideSelectListReq req) {
		List<SlidePageVo> slideSelectList = this.baseMapper.getSlideSelectList(req);
		List<String> animalCodes = slideSelectList.stream().map(SlidePageVo::getAnimalCode).distinct().sorted().collect(Collectors.toList());

		SlideSelectListVo slideSelectListVo = new SlideSelectListVo();
		slideSelectListVo.setAnimalCodes(animalCodes);
		return slideSelectListVo;
	}

	@Override
	public SlideSelectListVo getWaxCode(SlideSelectListReq req) {
		List<SlidePageVo> slideSelectList = this.baseMapper.getSlideSelectList(req);
		List<String> waxCode = slideSelectList.stream().map(SlidePageVo::getWaxCode).distinct().sorted().collect(Collectors.toList());

		SlideSelectListVo slideSelectListVo = new SlideSelectListVo();
		slideSelectListVo.setWaxCodes(waxCode);
		return slideSelectListVo;
	}

	@Override
	public SlideSelectListVo getGroupCode(SlideSelectListReq req) {
		List<SlidePageVo> slideSelectList = this.baseMapper.getSlideSelectList(req);
		List<String> groupCode = slideSelectList.stream().map(SlidePageVo::getGroupCode).distinct().sorted().collect(Collectors.toList());

		SlideSelectListVo slideSelectListVo = new SlideSelectListVo();
		slideSelectListVo.setGroupCodes(groupCode);
		return slideSelectListVo;
	}

	@Override
	public SlideSelectListVo getOrganCode(SlideSelectListReq req) {
		SlideSelectListVo slideSelectListVo = new SlideSelectListVo();
		List<SlideOrganTagVo> organCode = baseMapper.getOrganCode(req);
		slideSelectListVo.setOrganCodes(organCode);
		return slideSelectListVo;
	}

	@Override
	public boolean isAiSlideFinished(Long projectId) {
		return baseMapper.isAiSlideFinished(projectId);
	}

	@Override
	public R<String> aiAnalysis(AiAnalysisReq req) {
		// 加锁
		String key = "ai_analysis_projectId_" + req.getProjectId();
		Boolean result = this.redisTemplate.opsForValue().setIfAbsent(key, req.getProjectId());
		if (Boolean.FALSE.equals(result)) {
			return R.fail("处理中，请稍后");
		}
		try {
			// 查询项目信息
			Project project = this.projectMapper.selectById(req.getProjectId());
			// 校验：制片信息包含的蜡块编号是否覆盖项目内所有切片包含的所有蜡块编号
			// 查询项目切片蜡块号
			List<String> projectWaxCodes = this.baseMapper.selectWaxCodes(req.getProjectId());
			// 不为空才进行校验
			if (!CollectionUtils.isEmpty(projectWaxCodes)) {
				boolean match = false;
				// 查询制片信息蜡块编号
				LambdaQueryWrapper<Production> wrapper = new LambdaQueryWrapper<>();
				wrapper.eq(Production::getSpecialId, req.getProjectId());
				wrapper.eq(Production::getSpeciesId, project.getSpeciesId());
				List<Production> productions = this.productionMapper.selectList(wrapper);
				if (!CollectionUtils.isEmpty(productions)) {
					List<String> productionWaxCodes = productions.stream().map(Production::getWaxCode).collect(Collectors.toList());
					projectWaxCodes.removeAll(productionWaxCodes);
					if (CollectionUtils.isEmpty(projectWaxCodes)) {
						match = true;
					}
				}
				if (!match) {
					return R.fail("制片信息缺失，无法启动AI分析，请在项目配置中检查制片信息。");
				}
			}

			// 查询所有未分析的切片
			List<AiAnalysisBO> list = this.baseMapper.selectAiAnalysis(req.getProjectId());
			for (AiAnalysisBO bo : list) {
				try {
					log.info("脏器识别请求参数：{}", JSON.toJSONString(bo));
					String aiResult = HttpUtil.post(this.aiUrl, JSON.toJSONString(bo));
					log.info("脏器识别返回结果：{}", aiResult);
					// 更新切片状态为：脏器识别中
					Slide slide = new Slide();
					slide.setSlideId(bo.getSlideId());
					slide.setAiStatus(1);
					this.baseMapper.updateById(slide);
				} catch (Exception e) {
					log.info("脏器识别异常", e);
				}
			}
			return R.ok();
		} finally {
			// 释放锁
			this.redisTemplate.delete(key);
		}
	}

	@Override
	public boolean checkAiExecuted(Long projectId) {
		LambdaQueryWrapper<Slide> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(Slide::getProjectId, projectId);
		wrapper.eq(Slide::getDelFlag, "0");
		wrapper.gt(Slide::getAiStatus, 0);
		List<Slide> slides = this.baseMapper.selectList(wrapper);
		return !org.springframework.util.CollectionUtils.isEmpty(slides);
	}
}




