package cn.staitech.fr.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.AiForecast;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.Diagnosis;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.Measure;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.domain.Special;
import cn.staitech.fr.domain.SpecialAnnotationRel;
import cn.staitech.fr.domain.in.ChoiceSaveInVo;
import cn.staitech.fr.domain.in.SlideListQueryIn;
import cn.staitech.fr.domain.out.ImageListOutVO;
import cn.staitech.fr.domain.out.SlideListQueryOut;
import cn.staitech.fr.domain.out.SlideSelectBy;
import cn.staitech.fr.mapper.AiForecastMapper;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.DiagnosisMapper;
import cn.staitech.fr.mapper.JsonTaskMapper;
import cn.staitech.fr.mapper.MeasureMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.mapper.SpecialMapper;
import cn.staitech.fr.mapper.WaxBlockInfoMapper;
import cn.staitech.fr.service.GroupService;
import cn.staitech.fr.service.SlideService;
import cn.staitech.fr.utils.MessageSource;
import lombok.extern.slf4j.Slf4j;

/**
 * @author admin
 * @description 针对表【fr_slide(专题选片表)】的数据库操作Service实现
 * @createDate 2024-03-29 13:33:37
 */
@Service
@Slf4j
public class SlideServiceImpl extends ServiceImpl<SlideMapper, Slide>
implements SlideService {

	@Resource
	private GroupService groupService;
	@Resource
	private WaxBlockInfoMapper waxBlockInfoMapper;
	@Resource
	private SpecialMapper specialMapper;
	@Resource
	private SingleSlideMapper singleSlideMapper;
	@Resource
	private MeasureMapper measureMapper;
	@Resource
	private AiForecastMapper aiForecastMapper;
	@Resource
	private DiagnosisMapper diagnosisMapper;
	@Resource
	private JsonTaskMapper jsonTaskMapper;
	@Resource
	private AnnotationMapper annotationMapper;
	@Resource
	private SpecialAnnotationRelMapper specialAnnotationRelMapper;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R choiceSave(ChoiceSaveInVo req) {
		log.info("切片选择保存接口开始：");
		List<Slide> arrayList = new ArrayList<>();
		for (ImageListOutVO image : req.getImages()) {
			Slide slide = new Slide();
			slide.setCreateBy(SecurityUtils.getUserId());
			slide.setCreateTime(new Date());
			slide.setImageId(image.getImageId());
			slide.setSpecialId(req.getSpecialId());
			Slide extInfo = getExtInfo(image.getFileName(), slide, req.getSpecialId());
			arrayList.add(extInfo);
		}
		saveBatch(arrayList);
		return R.ok();
	}

	@Override
	public PageResponse<SlideListQueryOut> slideListQuery(SlideListQueryIn req) {
		log.info("专题下切片列表查询接口开始：");
		PageResponse<SlideListQueryOut> pageResponse = new PageResponse<>();
		Page<SlideListQueryOut> page = PageHelper.startPage(req.getPageNum(), req.getPageSize());
		List<SlideListQueryOut> resp = this.baseMapper.slideListQuery(req);
		pageResponse.setTotal(page.getTotal());
		pageResponse.setList(resp);
		pageResponse.setPages(page.getPages());
		return pageResponse;
	}

	@Override
	public SlideSelectBy pageImageCsvListVOBy(Long slideId) {
		return this.baseMapper.pageImageCsvListVOBy(slideId);
	}

	@Override
	public R deleteById(Long slideId) {
		log.info("删除切片接口开始：");
		//校验是否存在数据
		LambdaQueryWrapper<SingleSlide> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(SingleSlide::getSlideId, slideId);
		List<SingleSlide> singleSlides = singleSlideMapper.selectList(queryWrapper);
		if(singleSlides.size()>0){
			return R.fail(MessageSource.M("EXISTS_SINGLESLIDE_DATA"));
		}
		Slide slide = new Slide();
		slide.setSlideId(slideId);
		slide.setDelFlag(CommonConstant.NUMBER_1);
		this.baseMapper.updateById(slide);
		return R.ok();
	}

	private Slide getExtInfo(String fileName, Slide slide, Long specialId) {
		String[] s = fileName.split(" ");
		if (s.length != 3) {
			log.info("切片文件名格式错误：" + fileName);
			slide.setAnalyzeStatus(CommonConstant.NUMBER_1);
			slide.setProcessFlag(4);
			return slide;
		}
		String s1 = this.baseMapper.selectBySpecialId(specialId);
		if (!s[0].equals(s1)) {
			log.info("切片文件名格式错误：" + fileName);
			slide.setAnalyzeStatus(CommonConstant.NUMBER_1);
			slide.setProcessFlag(4);
			return slide;
		}
		slide.setAnimalCode(StringUtils.substringBeforeLast(s[1], "-"));
		slide.setWaxCode(StringUtils.substringAfterLast(s[1], "-"));
		//判断性别数据
		if (!CommonConstant.MALE.equals(s[2].substring(s[2].length() - 1)) &&
				!CommonConstant.FEMALE.equals(s[2].substring(s[2].length() - 1))) {
			log.info("切片文件名格式错误：" + fileName);
			slide.setAnalyzeStatus(CommonConstant.NUMBER_1);
			slide.setProcessFlag(4);
			return slide;
		}
		slide.setGenderFlag(s[2].substring(s[2].length() - 1));
		//判断组别
		/*Group byId = groupService.getById(s[2].substring(0, s[2].length() - 1));
        if (ObjectUtils.isEmpty(byId)) {
            log.info("切片文件名格式错误：" + fileName);
            slide.setAnalyzeStatus(CommonConstant.NUMBER_1);
            slide.setProcessFlag(4);
            return slide;
        }*/
		slide.setGroupCode(s[2].substring(0, s[2].length() - 1));
		Special special = specialMapper.selectById(specialId);
		slide.setOrgans(waxBlockInfoMapper.getOrganName(special.getTopicId(),special.getSpeciesId(),slide.getWaxCode(),s[2].substring(s[2].length() - 1)));
		return slide;
	}

	@Override
	public R deleteAll(Long specialId) {
		log.info("删除全部切片接口开始：");
		//fr_slide 切片删除
		QueryWrapper<Slide> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("special_id",specialId);
		//逻辑删除状态（0存在，1删除）
		queryWrapper.eq("del_flag",CommonConstant.NUMBER_0);
		List<Slide> slideList = list(queryWrapper);
		if(CollectionUtils.isNotEmpty(slideList)){
			//当前专题
			UpdateWrapper<Slide> updateWrapper = new UpdateWrapper<>();
			updateWrapper.eq("special_id", specialId);
			//修改状态
			Slide sd = new Slide();
			sd.setDelFlag("1");
			update(sd, updateWrapper);

			//slideId集合
			List<Long> slideIdList = slideList.stream().map(Slide::getSlideId).collect(Collectors.toList());
			//fr_single_slide 查询
			QueryWrapper<SingleSlide> querySsWrapper = new QueryWrapper<>();
			querySsWrapper.in("slide_id",slideIdList);
			List<SingleSlide> singleSlideList = singleSlideMapper.selectList(querySsWrapper);
			if(CollectionUtils.isNotEmpty(singleSlideList)){
				List<Long> singleSlideIdList = singleSlideList.stream().map(SingleSlide::getSingleId).collect(Collectors.toList());
				//fr_measure表数据处理
				QueryWrapper<Measure> wrapper = new QueryWrapper<>();
				wrapper.in("single_slide_id", singleSlideIdList);
				measureMapper.delete(wrapper);

				//fr_ai_forecast 查询
				QueryWrapper<AiForecast> queryAfWrapper = new QueryWrapper<>();
				queryAfWrapper.in("single_slide_id",singleSlideIdList);
				List<AiForecast> aiForecastList = aiForecastMapper.selectList(queryAfWrapper);
				if(CollectionUtils.isNotEmpty(aiForecastList)){
					//数据处理
					QueryWrapper<AiForecast> afWrapper = new QueryWrapper<>();
					afWrapper.in("single_slide_id", singleSlideIdList);
					aiForecastMapper.delete(afWrapper);
				}

				//fr_diagnosis 查询
				QueryWrapper<Diagnosis> queryDWrapper = new QueryWrapper<>();
				queryDWrapper.in("single_id",singleSlideIdList);
				List<Diagnosis> diagnosisList = diagnosisMapper.selectList(queryDWrapper);
				if(CollectionUtils.isNotEmpty(diagnosisList)){
					//数据处理
					QueryWrapper<Diagnosis> dWrapper = new QueryWrapper<>();
					dWrapper.in("single_id", singleSlideIdList);
					diagnosisMapper.delete(dWrapper);
				}

				//fr_json_task 查询 
				QueryWrapper<JsonTask> queryJWrapper = new QueryWrapper<>();
				queryJWrapper.in("single_id",singleSlideIdList);
				List<JsonTask> jsonTaskList = jsonTaskMapper.selectList(queryJWrapper);
				if(CollectionUtils.isNotEmpty(jsonTaskList)){
					//数据处理
					QueryWrapper<JsonTask> jWrapper = new QueryWrapper<>();
					jWrapper.in("single_id", singleSlideIdList);
					jsonTaskMapper.delete(jWrapper);
				}

				//postgre数据处理
				int batchSize = 2000;
				//fr_annotation
				QueryWrapper<Annotation> queryAnnowrapper = new QueryWrapper<>();
				queryAnnowrapper.in("slide_id",slideIdList);
				List<Annotation> annoList = annotationMapper.selectList(queryAnnowrapper);
				if(CollectionUtils.isNotEmpty(annoList)){
					//数据处理
					//					QueryWrapper<Annotation> aWrapper = new QueryWrapper<>();
					//					aWrapper.in("slide_id", slideIdList);
					//					annotationMapper.delete(aWrapper);
					// 如果待删除的singleSlideId少于或等于1000，则直接一次性删除

					if (CollectionUtils.isNotEmpty(slideIdList)) {
						if (slideIdList.size() <= batchSize) {
							annotationMapper.deleteBatchIds(slideIdList);
						}else{
							// 分批次删除，每次处理batchSize条
							for (int i = 0; i < slideIdList.size(); i += batchSize) {
								int end = Math.min(i + batchSize, slideIdList.size()); // 防止数组越界
								List<Long> idsBatch = slideIdList.subList(i, end); // 获取当前批次的ID
								annotationMapper.deleteBatchIds(slideIdList);
							}
						}
					}
				}
				//查询专题和fr_ai_annotation_X管理
				LambdaQueryWrapper<SpecialAnnotationRel> SpecialQueryWrapper = new LambdaQueryWrapper<>();
				SpecialQueryWrapper.eq(SpecialAnnotationRel::getSpecialId, specialId);
				SpecialAnnotationRel annotationRel = specialAnnotationRelMapper.selectOne(SpecialQueryWrapper);
				Long aiSequenceNumber = annotationRel.getSequenceNumber();
				if(null != aiSequenceNumber){
					//fr_ai_annotation_X
					Annotation annotation = new Annotation();
					annotation.setSequenceNumber(aiSequenceNumber);
					annotation.setSingleSlideIdList(singleSlideIdList);
					List<Annotation> aiAnnoList = annotationMapper.aiSelectListBy(annotation);
					// 如果待删除的singleSlideId少于或等于1000，则直接一次性删除
					if (CollectionUtils.isNotEmpty(aiAnnoList)) {
						List<Long> annoIdList = aiAnnoList.stream().map(Annotation::getAnnotationId).collect(Collectors.toList());
						if (aiAnnoList.size() <= batchSize) {
							annotationMapper.batchDeleteBySsIds(annoIdList);
						}else{
							// 分批次删除，每次处理batchSize条
							for (int i = 0; i < annoIdList.size(); i += batchSize) {
								int end = Math.min(i + batchSize, annoIdList.size()); // 防止数组越界
								List<Long> idsBatch = annoIdList.subList(i, end); // 获取当前批次的ID
								annotationMapper.batchDeleteBySsIds(idsBatch);
							}
						}
					}
				}
				//end fr_single_slide表singleSlideList删除
				QueryWrapper<SingleSlide> ssWrapper = new QueryWrapper<>();
				ssWrapper.in("single_id", singleSlideIdList);
				singleSlideMapper.delete(ssWrapper);
			}
		}
		return R.ok();

	}


}




