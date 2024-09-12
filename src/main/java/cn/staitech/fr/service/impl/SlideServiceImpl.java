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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import cn.hutool.core.util.ObjectUtil;
import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.domain.Special;
import cn.staitech.fr.domain.in.ChoiceSaveInVo;
import cn.staitech.fr.domain.in.SlideListQueryIn;
import cn.staitech.fr.domain.out.ImageListOutVO;
import cn.staitech.fr.domain.out.SlideListQueryOut;
import cn.staitech.fr.domain.out.SlideSelectBy;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.SlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.mapper.SpecialMapper;
import cn.staitech.fr.mapper.WaxBlockInfoMapper;
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
	private WaxBlockInfoMapper waxBlockInfoMapper;
	@Resource
	private SpecialMapper specialMapper;

	//	@Resource
	//	private SingleSlideMapper singleSlideMapper;
	//	@Resource
	//	private AiForecastMapper aiForecastMapper;
	//	@Resource
	//	private DiagnosisMapper diagnosisMapper;
	//	@Resource
	//	private JsonTaskMapper jsonTaskMapper;
	//	@Resource
	//	private JsonFileMapper jsonFileMapper;

//	@Resource
//	private MeasureMapper measureMapper;

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
			arrayList.add(slide);
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
		Slide slide = new Slide();
		slide.setSlideId(slideId);
		slide.setDelFlag(CommonConstant.NUMBER_1);
		this.baseMapper.updateById(slide);
		return R.ok();
	}


	@Override
	public R deleteAll(Long specialId,Long slideId) {
		log.info("删除全部切片接口开始：");
		//fr_slide 切片删除
		QueryWrapper<Slide> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq(ObjectUtil.isNotEmpty(specialId),"special_id",specialId);
		queryWrapper.eq(ObjectUtil.isNotEmpty(slideId),"slide_id",slideId);
		//逻辑删除状态（0存在，1删除）
		queryWrapper.eq("del_flag",CommonConstant.NUMBER_0);
		List<Slide> slideList = list(queryWrapper);
		if(CollectionUtils.isNotEmpty(slideList)){
			//当前专题
			UpdateWrapper<Slide> updateWrapper = new UpdateWrapper<>();
			updateWrapper.eq(ObjectUtil.isNotEmpty(specialId),"special_id", specialId);
			updateWrapper.eq(ObjectUtil.isNotEmpty(slideId),"slide_id",slideId);
			//修改状态
			Slide sd = new Slide();
			sd.setDelFlag("1");
			update(sd, updateWrapper);

			//slideId集合
//			List<Long> slideIdList = slideList.stream().map(Slide::getSlideId).collect(Collectors.toList());
			//fr_measure表数据处理
//			QueryWrapper<Measure> queryAmWrapper = new QueryWrapper<>();
//			queryAmWrapper.in("single_slide_id",slideList);
//			List<Measure> amList = measureMapper.selectList(queryAmWrapper);
//			if(CollectionUtils.isNotEmpty(amList)){
//				QueryWrapper<Measure> wrapper = new QueryWrapper<>();
//				wrapper.in("single_slide_id", slideList);
//				measureMapper.delete(wrapper);
//			}


			//postgre数据处理
//			int batchSize = 2000;
//			//fr_annotation
//			QueryWrapper<Contour> queryAnnowrapper = new QueryWrapper<>();
//			queryAnnowrapper.in("slide_id",slideIdList);
//			List<Contour> annoList = annotationMapper.selectList(queryAnnowrapper);
//			if(CollectionUtils.isNotEmpty(annoList)){
//				List<Long> annoIdList = annoList.stream().map(Contour::getAnnotationId).collect(Collectors.toList());
//				//数据处理
//				// 如果待删除的singleSlideId少于或等于batchSize，则直接一次性删除
//				if (CollectionUtils.isNotEmpty(annoIdList)) {
//					if (annoIdList.size() <= batchSize) {
//						annotationMapper.deleteBatchIds(annoIdList);
//					}else{
//						// 分批次删除，每次处理batchSize条
//						for (int i = 0; i < annoIdList.size(); i += batchSize) {
//							int end = Math.min(i + batchSize, annoIdList.size()); // 防止数组越界
//							List<Long> idsBatch = annoIdList.subList(i, end); // 获取当前批次的ID
//							annotationMapper.deleteBatchIds(idsBatch);
//						}
//					}
//				}
//			}
//			//查询专题和fr_ai_annotation_X管理
//			QueryWrapper<SpecialAnnotationRel> specialQueryWrapper = new QueryWrapper<>();
//			specialQueryWrapper.in("special_id",specialId);
//			SpecialAnnotationRel annotationRel = specialAnnotationRelMapper.selectOne(specialQueryWrapper);
//			Long aiSequenceNumber = annotationRel.getSequenceNumber();
//			if(null != aiSequenceNumber){
//				//fr_ai_annotation_X
//				Contour annotation = new Contour();
//				annotation.setSequenceNumber(aiSequenceNumber);
//				annotation.setSingleSlideIdList(slideList);
//				List<Contour> aiAnnoList = annotationMapper.aiSelectListBy(annotation);
//				// 如果待删除的singleSlideId少于或等于batchSize，则直接一次性删除
//				if (CollectionUtils.isNotEmpty(aiAnnoList)) {
//					List<Long> annoIdList = aiAnnoList.stream().map(Contour::getContourId).collect(Collectors.toList());
//					if (aiAnnoList.size() <= batchSize) {
//						Map<String,Object> parm = new HashMap<String, Object>();
//						parm.put("list", annoIdList);
//						parm.put("sequenceNumber", aiSequenceNumber);
//						contourMapper.batchDeleteBySsIds(parm);
//					}else{
//						// 分批次删除，每次处理batchSize条
//						for (int i = 0; i < annoIdList.size(); i += batchSize) {
//							int end = Math.min(i + batchSize, annoIdList.size()); // 防止数组越界
//							List<Long> idsBatch = annoIdList.subList(i, end); // 获取当前批次的ID
//							Map<String,Object> parm = new HashMap<String, Object>();
//							parm.put("list", idsBatch);
//							parm.put("sequenceNumber", aiSequenceNumber);
//							contourMapper.batchDeleteBySsIds(parm);
//						}
//					}
//				}
//			}
		}
		return R.ok();

	}


}




