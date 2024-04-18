package cn.staitech.fr.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.config.MapConstant;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.domain.WaxBlockInfo;
import cn.staitech.fr.domain.in.ResultCorrectionIn;
import cn.staitech.fr.domain.in.SplitVerificationQueryIn;
import cn.staitech.fr.domain.out.SplitVerificationOut;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.SlideMapper;
import cn.staitech.fr.service.SlideService;
import cn.staitech.fr.service.SplitVerificationService;
import cn.staitech.fr.service.WaxBlockInfoService;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @ClassName: SplitVerificationServiceServiceImpl
 * @Description:
 * @author wanglibei
 * @date 2024年4月3日
 * @version V1.0
 */
@Slf4j
@Service
public class SplitVerificationServiceServiceImpl implements SplitVerificationService {
	@Resource
	private SlideService slideService;

	@Resource
	private SlideMapper slideMapper;

	@Resource
	private AnnotationMapper annotationMapper;

	@Resource
	private WaxBlockInfoService waxBlockInfoService;

	@Override
	public PageResponse<SplitVerificationOut>  getList(SplitVerificationQueryIn req) {
//				Long organizationId = 6L;
		Long organizationId = SecurityUtils.getLoginUser().getSysUser().getOrganizationId();
		PageResponse<SplitVerificationOut> pageResponse = new PageResponse<>();
		//查看切片明细  0：否  1：是
		int detailType = req.getDetailType();
		if(detailType == 0){
			pageResponse = SplitVerificationCount(req,organizationId);
		}else{
			pageResponse =  SplitVerificationDetailCount(req,organizationId);
		}

		return pageResponse;
	}

	//非明细统计
	private  PageResponse<SplitVerificationOut> SplitVerificationCount(SplitVerificationQueryIn req,Long organizationId){
		PageResponse<SplitVerificationOut> pageResponse = new PageResponse<>();
		String reqAnimalCode = req.getAnimalCode();
		//只看核对异常数据  0：全部  1：只看异常数据
		int reqCheckType = 0;
		if(null != req.getCheckType()){
			reqCheckType = req.getCheckType();
		}
		int pageSize = req.getPageSize(); // 每页显示的条目数量
		int currentPage = req.getPageNum(); // 当前页码

		List<SplitVerificationOut> dataList = new ArrayList<SplitVerificationOut>();

		QueryWrapper<Slide> queryWrapper = new QueryWrapper<>();
		queryWrapper.select("DISTINCT( animal_code )  animal_code");
		queryWrapper.eq("special_id",req.getSpecialId());
		//处理状态（0：待切图,1：切图中,2：已切图 3：切图失败）
		queryWrapper.eq("process_flag",2);
		//:核对状态 0：初始 1：正确 2：修正正常 3：错误 
		queryWrapper.gt("check_status",0);
		//切片名称解析，0：成功；1：失败
		queryWrapper.eq("analyze_status","1");
		if(StringUtils.isNotEmpty(reqAnimalCode)){
			queryWrapper.like("animal_code",reqAnimalCode);
		}

		//核对状态 0：初始 1：正确 2：修正正常 3：错误 
		if(reqCheckType ==  1){
			queryWrapper.eq("check_status",3);
		}

		Long reqCategoryId = req.getCategoryId();
		if(null != reqCategoryId){
			Annotation annotation = new Annotation();
			annotation.setCategoryId(reqCategoryId);
			//加专题所有id
			//查下所属专题的所有切片id
			List<Long> querySlideIdList = new ArrayList<>();
			QueryWrapper<Slide> animalWrapper = new QueryWrapper<>();
			animalWrapper.eq("special_id",req.getSpecialId());
			//切片名称解析，0：成功；1：失败
			animalWrapper.eq("analyze_status","1");
			List<Slide> slideDataList = slideMapper.selectList(animalWrapper);
			if(CollectionUtils.isNotEmpty(slideDataList)){
				for(Slide slide:slideDataList){
					querySlideIdList.add(slide.getSlideId());
				}
				annotation.setSlideIdList(querySlideIdList);
			}
			
			List<Annotation> annoList = annotationMapper.selectListBy(annotation);
			List<Long> annoSlideIdList = annoList.stream().map(Annotation::getSlideId).collect(Collectors.toList());
			if(CollectionUtils.isNotEmpty(annoSlideIdList)){
				req.setSlideIdList(annoSlideIdList);
			}
			String oCategory = organizationId.toString()+reqCategoryId.toString();
			String categoryName = MapConstant.getCategory(oCategory);
//			queryWrapper.like("organs",categoryName);
			
			queryWrapper.like("organs", categoryName);
			if(CollectionUtils.isNotEmpty(annoSlideIdList)){
				queryWrapper.or().in("slide_id", annoSlideIdList);
			}
			
		}



		List<Slide> slideList = slideService.list(queryWrapper);

		//动物编号处理
		if(CollectionUtils.isNotEmpty(slideList)){
			for(Slide slide:slideList){
				String animalCode = slide.getAnimalCode();
				//查询当前专题下当前动物编号的所有切片
				QueryWrapper<Slide> animalWrapper = new QueryWrapper<>();
				animalWrapper.eq("special_id",req.getSpecialId());
				animalWrapper.eq("animal_code",animalCode);
				//处理状态（0：待切图,1：切图中,2：已切图 3：切图失败）
				animalWrapper.eq("process_flag",2);
				//:核对状态 0：初始 1：正确 2：修正正常 3：错误 
				animalWrapper.gt("check_status",0);
				List<Slide> animalSlideList = slideService.list(animalWrapper);
				//切图结果（0：正确 1：错误）
				int allCheckStatus = 0;
				if(CollectionUtils.isNotEmpty(animalSlideList)){
					//切图结果==》按照动物编号统计汇总
					Map<Integer, List<Slide>> checkMap = animalSlideList.stream().collect(Collectors.groupingBy(Slide::getCheckStatus));

					if(null != checkMap && checkMap.containsKey(3)){
						allCheckStatus = 1;
					}
					
					Map<String, Long> waxCategoryMap = new HashMap<String, Long>();
					for(Slide slidePer:animalSlideList){
						//切图结果==》按照动物编号统计汇总
						List<WaxBlockInfo> waxinfoList = waxBlockInfoService.getWaxBlockInfoList(slidePer.getSlideId(), slidePer.getWaxCode(),slidePer.getGenderFlag());
						//处理蜡块信息
						if(CollectionUtils.isNotEmpty(waxinfoList)){
							for(WaxBlockInfo info:waxinfoList){
								String organName = info.getOrganName();
								Integer organNumber = info.getOrganNumber();
								if(waxCategoryMap.isEmpty()){
									waxCategoryMap.put(organName, organNumber.longValue());
								}else{
									if(waxCategoryMap.containsKey(organName)){
										Long hasOrganNumber = waxCategoryMap.get(organName);
										waxCategoryMap.put(organName, hasOrganNumber+organNumber.longValue());
									}else{
										waxCategoryMap.put(organName, organNumber.longValue());
									}
								}

							}
						}
					}
					//蜡块表脏器信息==》按照动物脏器统计汇总==>底层都是按照字典顺序进行排序(https://blog.csdn.net/Rcain_R/article/details/136692093)
					waxCategoryMap = waxCategoryMap.entrySet().stream()
							.sorted(Map.Entry.comparingByKey())
							.collect(Collectors.toMap(
									Map.Entry::getKey,
									Map.Entry::getValue,
									// 解决可能存在的键冲突问题，默认保留第一个值
									(oldValue, newValue) -> oldValue,
									// 提供一个新的TreeMap实例作为收集器，用于保持排序
									() -> new TreeMap<>() 
									));
					

					//切图脏器信息==》按照切片脏器统计汇总
					List<Long> annoSlideIdList = animalSlideList.stream().map(Slide::getSlideId).collect(Collectors.toList());
					List<Annotation>  annoList = annotationMapper.getAnnoListByParm(annoSlideIdList);
					Map<Long, Long> categoryCountGroupedBycategory = annoList.stream().collect(Collectors.groupingBy(Annotation::getCategoryId, Collectors.counting()));
					//AI 切图数据处理
					Map<String, Long> annoCategoryMap = new HashMap<String, Long>();
					if(null != categoryCountGroupedBycategory){
						//根据标签id获取脏器名称
						for (Map.Entry<Long, Long> entry : categoryCountGroupedBycategory.entrySet()) {
							Long categoryId = entry.getKey();
							Long categoryCount = entry.getValue();
							String oCategory = organizationId.toString()+categoryId.toString();
							String categoryFullName = MapConstant.getCategory(oCategory);
							if(StringUtils.isNotEmpty(categoryFullName)){
								annoCategoryMap.put(categoryFullName, categoryCount);
							}
						}
						annoCategoryMap = annoCategoryMap.entrySet().stream()
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(
										Map.Entry::getKey,
										Map.Entry::getValue,
										// 解决可能存在的键冲突问题，默认保留第一个值
										(oldValue, newValue) -> oldValue,
										// 提供一个新的TreeMap实例作为收集器，用于保持排序
										() -> new TreeMap<>() 
										));

					}

					SplitVerificationOut svOut = new SplitVerificationOut();
					svOut.setAnimalCode(animalCode);
					svOut.setProcessFlag(allCheckStatus);
					svOut.setWaxOrgan(waxCategoryMap);
					svOut.setAnnoOrgan(annoCategoryMap);
					dataList.add(svOut);
				}
			}


			//排序说明：一级，第一优先级为切图图结果不一致的动物编号靠前，按动物编号正序排序；二级，第一优先级为切图图结果不一致的动物编号靠前，按切片编号正序排序处理
			List<SplitVerificationOut> sortedList = dataList.stream()
					.sorted(Comparator.comparing(SplitVerificationOut::getProcessFlag).reversed())
					.collect(Collectors.toList());

			sortedList = dataList.stream()
					.sorted(Comparator.comparing(SplitVerificationOut::getProcessFlag).reversed())
					.collect(Collectors.toList());

			int total = 0;
			if(CollectionUtils.isNotEmpty(sortedList)){
				total = sortedList.size();
			}

			// 计算总共有多少页
			int totalPages = 0;
			if(total != 0){
				totalPages = (total + pageSize - 1) / pageSize;
			}

			// 计算起始索引和结束索引
			int startIndex = (currentPage - 1) * pageSize;
			int endIndex = Math.min(startIndex + pageSize, sortedList.size());
			//System.out.println("startIndex：" + startIndex+" endIndex:"+endIndex);
			// 提取指定范围内的元素作为当前页的结果集合
			List<SplitVerificationOut> result = sortedList.subList(startIndex, endIndex);

			if(CollectionUtils.isEmpty(result)){
				result = new ArrayList<>();
			}

			pageResponse.setTotal(total);
			pageResponse.setPages(totalPages);
			pageResponse.setList(result);
			pageResponse.setPageNum(currentPage);
			pageResponse.setPageSize(pageSize);

		}else{
			int total = 0;
			int totalPages = 0;
			List<SplitVerificationOut> result = new ArrayList<>();
			pageResponse.setTotal(total);
			pageResponse.setPages(totalPages);
			pageResponse.setList(result);
			pageResponse.setPageNum(currentPage);
			pageResponse.setPageSize(pageSize);
		}
		return pageResponse;
	}


	//明细统计
	private  PageResponse<SplitVerificationOut> SplitVerificationDetailCount(SplitVerificationQueryIn req,Long organizationId){
		//查找符合条件的AI
		Long categoryId = req.getCategoryId();
		if(null != categoryId){
			Annotation annotation = new Annotation();
			annotation.setCategoryId(categoryId);
			List<Long> slideIdList = new ArrayList<>();
			//查下所属专题的所有切片id
			QueryWrapper<Slide> animalWrapper = new QueryWrapper<>();
			animalWrapper.eq("special_id",req.getSpecialId());
			//切片名称解析，0：成功；1：失败
			animalWrapper.eq("analyze_status","1");
			List<Slide> slideDataList = slideMapper.selectList(animalWrapper);
			if(CollectionUtils.isNotEmpty(slideDataList)){
				for(Slide slide:slideDataList){
					slideIdList.add(slide.getSlideId());
				}
				annotation.setSlideIdList(slideIdList);
			}
			List<Annotation> annoList = annotationMapper.selectListBy(annotation);
			List<Long> annoSlideIdList = annoList.stream().map(Annotation::getSlideId).collect(Collectors.toList());
			if(CollectionUtils.isNotEmpty(annoSlideIdList)){
				req.setSlideIdList(annoSlideIdList);
			}
			String oCategory = organizationId.toString()+categoryId.toString();
			String categoryName = MapConstant.getCategory(oCategory);
			if(StringUtils.isNotEmpty(categoryName)){
				req.setCategoryName(categoryName);
			}
		}

		PageResponse<SplitVerificationOut> pageResponse = new PageResponse<>();
		Page<SplitVerificationOut> page = PageHelper.startPage(req.getPageNum(), req.getPageSize());

		List<SplitVerificationOut> slideList = slideMapper.getVerificationSlideListQuery(req);
		if(CollectionUtils.isNotEmpty(slideList)){
			for(SplitVerificationOut out:slideList){
				Long slideId = out.getSlideId();
				Slide slide = slideMapper.selectById(slideId);
				//修正标识（ 0：初始 1：正确 2：修正正常 3：错误 ）
				int checkStatus = out.getCheckStatus();
				//切图结果（0：正确 1：错误）
				int processFlag = 1;
				if(checkStatus == 1 || checkStatus == 2){
					processFlag = 2;
				}
				out.setProcessFlag(processFlag);
				//查询anno统计信息
				Annotation queryAnno = new Annotation();
				queryAnno.setSlideId(slideId);
				queryAnno.setContourType(2L);
				List<Annotation>  annoList = annotationMapper.selectListBy(queryAnno);
				Map<Long, Long> categoryCountGroupedBycategory = annoList.stream().collect(Collectors.groupingBy(Annotation::getCategoryId, Collectors.counting()));
				//AI 切图数据处理
				Map<String, Long> annoCategoryMap = new HashMap<String, Long>();
				if(null != categoryCountGroupedBycategory){
					//根据标签id获取脏器名称
					for (Map.Entry<Long, Long> entry : categoryCountGroupedBycategory.entrySet()) {
						Long categoryIdP = entry.getKey();
						Long categoryCount = entry.getValue();
						String oCategory = organizationId.toString()+categoryIdP.toString();
						String categoryFullName = MapConstant.getCategory(oCategory);
						if(StringUtils.isNotEmpty(categoryFullName)){
							annoCategoryMap.put(categoryFullName, categoryCount);
						}
					}
				}
				out.setAnnoOrgan(annoCategoryMap);

				//切片蜡片信息汇总
				//查询当前专题下当前动物编号的所有切片
				String genderFlag = slide.getGenderFlag();
				Map<String, Long> waxCategoryMap = new HashMap<String, Long>();
				//切图结果==》按照动物编号统计汇总
				List<WaxBlockInfo> waxinfoList = waxBlockInfoService.getWaxBlockInfoList(slideId, slide.getWaxCode(),genderFlag);
				//处理蜡块信息
				if(CollectionUtils.isNotEmpty(waxinfoList)){
					for(WaxBlockInfo info:waxinfoList){
						String organName = info.getOrganName();
						Integer organNumber = info.getOrganNumber();
						waxCategoryMap.put(organName, organNumber.longValue());
					}
				}
				out.setWaxOrgan(waxCategoryMap);
			}
		}
		pageResponse.setTotal(page.getTotal());
		if(CollectionUtils.isEmpty(slideList)){
			slideList = new ArrayList<>();
		}
		pageResponse.setList(slideList);
		pageResponse.setPageNum(req.getPageNum());
		pageResponse.setPageSize(req.getPageSize());
		pageResponse.setPages(page.getPages());

		return pageResponse;
	}

	@Override
	public void updateResult(ResultCorrectionIn req) {
		Long slideId = req.getSlideId();
		Slide slide = new Slide();
		slide.setSlideId(slideId);

		//修正状态  0：修正  1：取消修正
		if(req.getCorrectionStatus() == 0){
			//核对状态 0：初始 1：正确 2：修正正常 3：错误 
			slide.setCheckStatus(2);
		}else{
			slide.setCheckStatus(3);
		}
		slide.setCheckBy(SecurityUtils.getLoginUser().getSysUser().getUserId());
		slide.setCheckTime(new Date());
		slideService.updateById(slide);
		//动物号处理
		//检查当前动物号所属的所有切片是否正常
		Slide oldSlide = slideService.getById(slideId);
		//动物号
		String animalCode = oldSlide.getAnimalCode();
		Long specialId = oldSlide.getSpecialId();

		QueryWrapper<Slide> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("animal_code", animalCode);
		queryWrapper.eq("special_id", specialId);
		List<Slide> queryList = slideService.list(queryWrapper);
		// 按照checkStatus属性分组到Map<String, List<Slide>>
		Map<Integer, List<Slide>> mapByCheckStatus = queryList.stream().collect(Collectors.groupingBy(Slide::getCheckStatus));
		if(null !=mapByCheckStatus&& !mapByCheckStatus.isEmpty()){
			UpdateWrapper<Slide> updateWrapper = new UpdateWrapper<>();
			updateWrapper.eq("animal_code", animalCode);
			updateWrapper.eq("special_id", specialId);
			Slide sd = new Slide();
			//当前动物号检查状态 核对状态 0：初始 1：正确 2：修正正常 3：错误
			if(mapByCheckStatus.containsKey(2)){
				sd.setAnimalCheckStatus(2);
			}else{
				sd.setAnimalCheckStatus(1);
			}
			slideService.update(sd, updateWrapper);
		}
	}





}
