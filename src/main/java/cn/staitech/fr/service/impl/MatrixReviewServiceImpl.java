package cn.staitech.fr.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.constant.Container;
import cn.staitech.fr.domain.AiForecast;
import cn.staitech.fr.domain.Category;
import cn.staitech.fr.domain.PageDataResponse;
import cn.staitech.fr.domain.SingleOrganNumber;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.domain.Special;
import cn.staitech.fr.domain.in.AiDownloadIn;
import cn.staitech.fr.domain.in.AlgorithmIn;
import cn.staitech.fr.domain.in.MatrixReviewEditIn;
import cn.staitech.fr.domain.in.MatrixReviewListIn;
import cn.staitech.fr.domain.in.SingleSlideAdjacent;
import cn.staitech.fr.domain.out.*;
import cn.staitech.fr.feign.PythonOrganRecognitionService;
import cn.staitech.fr.mapper.AiForecastMapper;
import cn.staitech.fr.mapper.DiagnosisMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SlideMapper;
import cn.staitech.fr.mapper.SpecialMapper;
import cn.staitech.fr.service.CategoryService;
import cn.staitech.fr.service.MatrixReviewService;
import cn.staitech.fr.utils.DateUtils;
import cn.staitech.fr.utils.ExportPdfUtils;
import cn.staitech.fr.utils.LanguageUtils;
import cn.staitech.fr.utils.MathUtils;
import cn.staitech.fr.vo.annotation.AiAlgorithm;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.deepoove.poi.data.PictureRenderData;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Author wudi
 * @Date 2024/4/10 15:54
 * @desc
 */
@Service
@Slf4j
public class MatrixReviewServiceImpl implements MatrixReviewService {
	@Resource
	private SlideMapper slideMapper;
	@Autowired
	private SpecialMapper specialMapper;

	@Resource
	private SingleSlideMapper singleSlideMapper;

	@Resource
	private DiagnosisMapper diagnosisMapper;

	@Resource
	private HttpServletResponse response;

	@Resource
	private PythonOrganRecognitionService pythonService;

	@Resource
	private AiForecastMapper aiForecastMapper;

	@Resource
	private CategoryService categoryService;


	@Value("${waxPath}")
	private String waxPath;

	@Override
	public R<List<MatrixReviewOut>> groupList(Long specialId) {
		log.info("对照组数据查询接口开始：");
		List<MatrixReviewOut> resp = new ArrayList<>();
		LambdaQueryWrapper<Slide> wrapper = new LambdaQueryWrapper<>();
		wrapper.select(Slide::getGroupCode);
		wrapper.eq(Slide::getSpecialId, specialId);
		wrapper.eq(Slide::getDelFlag, CommonConstant.NUMBER_0);
		wrapper.isNotNull(Slide::getGroupCode);
		wrapper.groupBy(Slide::getGroupCode);
		List<Slide> slideList = slideMapper.selectList(wrapper);
		if (CollectionUtils.isNotEmpty(slideList)) {
			resp = slideList.stream().map(e -> {
				MatrixReviewOut matrixReviewOut = new MatrixReviewOut();
				matrixReviewOut.setGroupId(e.getGroupCode());
				matrixReviewOut.setGroupCode(e.getGroupCode());
				return matrixReviewOut;
			}).collect(Collectors.toList());
		}
		return R.ok(resp);
	}

	@Override
	public R edit(MatrixReviewEditIn req) {
		log.info("对照组数据编辑接口开始：");
		Special special = new Special();
		special.setSpecialId(req.getSpecialId());
		special.setControlGroup(req.getGroupId());
		specialMapper.updateById(special);
		return R.ok();
	}

	@Override
	public PageResponse<MatrixReviewListOut> getMatrixReview(MatrixReviewListIn req) {
		log.info("阅片列表单切片维度接口查询开始：");
		//创建响应
		PageResponse resp = new PageResponse();
		//分页查询
		Page<MatrixReviewListOut> page = PageHelper.startPage(req.getPageNum(), req.getPageSize());
		List<MatrixReviewListOut> waxList = slideMapper.getMatrixReview(req);
		if (CollectionUtils.isEmpty(waxList)) {
			resp.setTotal(page.getTotal());
			resp.setList(waxList);
			resp.setPages(page.getPages());
			return resp;
		}
		List<Long> slideIds = waxList.stream().map(MatrixReviewListOut::getSlideId).distinct().collect(Collectors.toList());
		List<SingleOrganNumber> singleOrganNumbers = singleSlideMapper.selectNumber(slideIds, req.getCategoryId());
		if (CollectionUtil.isEmpty(singleOrganNumbers)) {
			resp.setTotal(page.getTotal());
			resp.setList(waxList);
			resp.setPages(page.getPages());
			return resp;
		}
		Map<Long, Map<Long, Long>> map = singleOrganNumbers.stream().collect(Collectors.groupingBy(SingleOrganNumber::getSlideId, Collectors.toMap(SingleOrganNumber::getCategoryId, SingleOrganNumber::getOrganNumber)));
		waxList = waxList.stream().peek(p -> p.setOrganNumber(ObjectUtil.isEmpty(map.get(p.getSlideId())) ? 0L : map.get(p.getSlideId()).get(p.getCategoryId()))).collect(Collectors.toList());
		resp.setTotal(page.getTotal());
		resp.setList(waxList);
		resp.setPages(page.getPages());
		return resp;
	}


	@Override
	public HashMap<String, SingleSlideSelectBy> SingleSlideAdjacent(SingleSlideAdjacent req) {
		List<MatrixReviewListOut> waxList = slideMapper.SingleSlideAdjacent(req);
		// 根据下标查询出附近的数据
		AtomicInteger index = new AtomicInteger(0);
		waxList.stream()
		//指定匹配逻辑
		.filter(s -> {
			//每比对一个元素，数值加1
			index.getAndIncrement();
			return s.getSingleId().equals(req.getSingleSlideId());
		}).findFirst();
		HashMap<String, SingleSlideSelectBy> map = new HashMap<>();

		int indexsx = index.get() - 1;
		if (waxList.size() > 0) {
			if (indexsx == 0) {
				if (waxList.size() > 1) {
					map.put("prev", null);
					Long singleSlideId = waxList.get(indexsx + 1).getSingleId();
					SingleSlideSelectBy slideSelectBy = singleSlideMapper.singleSlideBy(singleSlideId);
					map.put("next", slideSelectBy);
				} else {
					map.put("prev", null);
					map.put("next", null);
				}
			} else if (waxList.size() - 1 == indexsx) {
				Long singleSlideId = waxList.get(indexsx - 1).getSingleId();
				SingleSlideSelectBy slideSelectBy = singleSlideMapper.singleSlideBy(singleSlideId);
				map.put("prev", slideSelectBy);
				map.put("next", null);
			} else {
				Long singleSlideIdPrev = waxList.get(indexsx - 1).getSingleId();
				SingleSlideSelectBy slideSelectByPrev = singleSlideMapper.singleSlideBy(singleSlideIdPrev);
				map.put("prev", slideSelectByPrev);
				Long singleSlideIdNext = waxList.get(indexsx + 1).getSingleId();
				SingleSlideSelectBy slideSelectByNext = singleSlideMapper.singleSlideBy(singleSlideIdNext);
				map.put("next", slideSelectByNext);
			}
		} else {
			map.put("prev", null);
			map.put("next", null);
		}
		return map;
	}

	@Override
	public List<SingleSlideSelectBy> specialSlideList(SingleSlideAdjacent req){
		return singleSlideMapper.singleSlideList(req);
	}

	@Override
	public PageResponse<SelectImageSlideOut> selectSlideList(MatrixReviewListIn req) {
		PageResponse resp = new PageResponse();
		Page<SelectImageSlideOut> page = PageHelper.startPage(req.getPageNum(), req.getPageSize());
		List<SelectImageSlideOut> waxList = slideMapper.selectSlideList(req);
		resp.setTotal(page.getTotal());
		resp.setList(waxList);
		resp.setPages(page.getPages());
		return resp;
	}

	@Override
	public PageDataResponse<AnimalDimensionOut> animalList(MatrixReviewListIn req) {
		log.info("阅片列表单动物维度接口查询开始：");

		PageDataResponse<AnimalDimensionOut> resp = new PageDataResponse<>();
		AnimalDimensionOut ret = new AnimalDimensionOut();
		//查询动物
		LambdaQueryWrapper<Slide> wrapper2 = new LambdaQueryWrapper<>();
		wrapper2.select(Slide::getAnimalCode,Slide::getGroupCode,Slide::getGenderFlag);
		wrapper2.eq(Slide::getDelFlag, CommonConstant.NUMBER_0);
		wrapper2.eq(Slide::getSpecialId, req.getSpecialId());
		wrapper2.isNotNull(Slide::getAnimalCode);
		wrapper2.like(StringUtils.isNotEmpty(req.getAnimalCode()), Slide::getAnimalCode, req.getAnimalCode());
		wrapper2.like(StringUtils.isNotEmpty(req.getGroupCode()), Slide::getGroupCode, req.getGroupCode());
		wrapper2.groupBy(Slide::getAnimalCode,Slide::getGroupCode,Slide::getGenderFlag);
		Page<Slide> page = PageHelper.startPage(req.getPageNum(), req.getPageSize());
		List<Slide> slideLists = slideMapper.selectList(wrapper2);
		if (CollectionUtils.isEmpty(slideLists)) {
			resp.setTotal(page.getTotal());
			resp.setPageNum(req.getPageNum());
			resp.setPageSize(req.getPageSize());
			resp.setData(ret);
			resp.setPages(page.getPages());
		}
		//每一个动物处理
		List<AnimalDimensionData> retData = new ArrayList<>();
		for (Slide slideList : slideLists) {
			//查询基础数据
			LambdaQueryWrapper<Slide> wrapper = new LambdaQueryWrapper<>();
			wrapper.eq(Slide::getDelFlag, CommonConstant.NUMBER_0);
			wrapper.eq(Slide::getSpecialId, req.getSpecialId());
			wrapper.eq(Slide::getAnimalCode,slideList.getAnimalCode());
			wrapper.eq(Slide::getGroupCode,slideList.getGroupCode());
			wrapper.eq(Slide::getGenderFlag,slideList.getGenderFlag());
			List<Slide> slides = slideMapper.selectList(wrapper);
			//每个动物的脏器
			//数据
			AnimalDimensionData out = new AnimalDimensionData();
			out.setGroupCode(slideList.getGroupCode());
			out.setAnimalCode(slideList.getAnimalCode());
			out.setGenderFlag(slideList.getGenderFlag());
			List<OrgansData> organsData = slideMapper.selectRespData(slides);
			out.setOrgans(organsData);
			retData.add(out);
		}
		//表头
		List<Category> headList = slideMapper.selectHeadList(req.getSpecialId());
		ret.setDataList(retData);
		ret.setCategoryList(headList);
		resp.setTotal(page.getTotal());
		resp.setData(ret);
		resp.setPageNum(req.getPageNum());
		resp.setPageSize(req.getPageSize());
		resp.setPages(page.getPages());
		return resp;
	}

	@Override
	public void diagnosisDownload(AiDownloadIn req) throws Exception {
		log.info("诊断报告下载接口开始：");
		List<Long> ids = req.getIds();
		List<String> pdfName = new ArrayList<>();
		String topicName = "";
		for (Long id : ids) {
			ExportVO exportVO = singleSlideMapper.getExportVO(id);
			if (LanguageUtils.isEn()) {
				exportVO.setColorType(Container.COLOR_TYPE_EN.get(Integer.valueOf(exportVO.getColorType())));
			} else {
				exportVO.setColorType(Container.COLOR_TYPE.get(Integer.valueOf(exportVO.getColorType())));
			}
			List<ExportListVO> collect = diagnosisMapper.getExportListVO(id);
			exportVO.setList(collect);
			exportVO.setTable(collect);
			exportVO.setOrganizationName(diagnosisMapper.getOrganizationName(SecurityUtils.getLoginUser().getSysUser().getOrganizationId()));
			//会报错{"msg":"TemplateRenderPolicy render error","code":500}
			//exportVO.setImg(new PictureRenderData(800, 200, "D:/image/liangz.png"));
			exportVO.setImg(new PictureRenderData(800, 200, exportVO.getThumbUrl().replace("/file/statics", "/home/pat_saas")));
			String s = waxPath + exportVO.getFileName() + "+" + exportVO.getOrganName() + CommonConstant.WROD_FILE;
			//生成word
			ExportPdfUtils.exportFile(s, exportVO);
			//生成pdf
			//ExportPdfUtils.convertDocx2Pdf(s, s.replace(CommonConstant.WROD_FILE, CommonConstant.PDF_FILE));
			ExportPdfUtils.wordToPdf(s, s.replace(CommonConstant.WROD_FILE, CommonConstant.PDF_FILE));
			pdfName.add(s.replace(CommonConstant.WROD_FILE, CommonConstant.PDF_FILE));
			topicName = exportVO.getTopicName();
		}
		if (ids.size() > 1) {
			log.info("走的压缩包");
			ExportPdfUtils.writePdfZip(pdfName, response, topicName + DateUtils.getCurrentHHmmssString("yyyy-MM-dd HH:mm:ss") + CommonConstant.ZIP_FILE);

		} else {

			ExportPdfUtils.downloadLocal(pdfName.get(0), response);

		}
		for (String s1 : pdfName) {
			if (new File(s1).exists()) {
				FileUtils.delete(new File(s1));
				//FileUtils.delete(new File(s1.replace(CommonConstant.PDF_FILE, CommonConstant.WROD_FILE)));
			}

		}
		log.info("结束");
	}

	@Override
	public R<String> getControlGroup(Long specialId) {
		log.info("对照组获得接口开始：");
		Special special = specialMapper.selectById(specialId);
		return R.ok(special.getControlGroup());
	}

	@Override
	public void algorithmDownload(AiDownloadIn req) throws Exception {
		log.info("ai预测报告导出接口开始：");
		List<Long> ids = req.getIds();
		List<String> pdfName = new ArrayList<>();
		String topicName = "";
		//存放单脏器切片id和脏器id
		Map<Long,Long> categorys=new HashMap<>();
		//判断是不是存在对照组
		Special special = specialMapper.selectById(req.getSpecialId());
		if(StringUtils.isNotEmpty(special.getControlGroup())){
			LambdaQueryWrapper<SingleSlide> wrapper = new LambdaQueryWrapper<>();
			wrapper.in(SingleSlide::getSingleId,ids);
			List<SingleSlide> singleSlides = singleSlideMapper.selectList(wrapper);
			categorys =  singleSlides.stream().collect(Collectors.toMap(SingleSlide::getSingleId,SingleSlide::getCategoryId));
		}
		for (Long id : ids) {
			ExportAiVO exportVO = singleSlideMapper.getExportAiVO(id);

			if (LanguageUtils.isEn()) {
				exportVO.setColorType(Container.COLOR_TYPE_EN.get(Integer.valueOf(exportVO.getColorType())));
			} else {
				exportVO.setColorType(Container.COLOR_TYPE.get(Integer.valueOf(exportVO.getColorType())));
			}
			//算法结果数据填充
			LambdaQueryWrapper<AiForecast> wrapper = new LambdaQueryWrapper<>();
			wrapper.eq(AiForecast::getSingleSlideId, id);
			wrapper.eq(AiForecast::getStructType,CommonConstant.NUMBER_0);
			List<AiForecast> aiForecasts = aiForecastMapper.selectList(wrapper);
			List<ExportAiListVO> collect = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(aiForecasts)) {
				for (AiForecast aiForecast : aiForecasts) {
					ExportAiListVO exportAiListVO = new ExportAiListVO();
					BeanUtils.copyProperties(aiForecast, exportAiListVO);
					if(new BigDecimal(aiForecast.getResults()).compareTo(BigDecimal.ZERO)<0){
						exportAiListVO.setResults("?");
					}
					//范围数据
					if(StringUtils.isNotEmpty(special.getControlGroup())){
						String genderFlag=singleSlideMapper.getGender(id);
						//setRang(aiForecast.getQuantitativeIndicators(),special,id,exportAiListVO,categorys);
						if(StringUtils.isNotEmpty(genderFlag)){
							setReferenceScope(special, id, exportAiListVO, categorys,genderFlag);
						}

					}
					collect.add( exportAiListVO);
				}
			}
			exportVO.setList(collect);
			exportVO.setTable(collect);
			//算法详情
			AipreAirepostOut aiForecastBySingle = aiForecastMapper.getAiForecastBySingle(id);
			if(ObjectUtils.isNotEmpty(aiForecastBySingle)){
				exportVO.setAlgorithmName(aiForecastBySingle.getAlgorithmName());
				exportVO.setModelVersion(aiForecastBySingle.getModelVersion());
				exportVO.setStartTime(aiForecastBySingle.getStartTime());
				exportVO.setWasteTime(aiForecastBySingle.getWasteTime());
			}

			exportVO.setOrganizationName(diagnosisMapper.getOrganizationName(SecurityUtils.getLoginUser().getSysUser().getOrganizationId()));
			String s = waxPath + "AI" + File.separator + exportVO.getFileName() + "+" + exportVO.getOrganName() + CommonConstant.WROD_FILE;
			File file = new File(waxPath + "AI" + File.separator);
			if(!file.exists()&&!file.isDirectory()){
				file.mkdir();
			}
			//生成word
			ExportPdfUtils.exportAiFile(s, exportVO);
			//生成pdf
			//ExportPdfUtils.convertDocx2Pdf(s, s.replace(CommonConstant.WROD_FILE, CommonConstant.PDF_FILE));
			ExportPdfUtils.wordToPdf(s, s.replace(CommonConstant.WROD_FILE, CommonConstant.PDF_FILE));
			pdfName.add(s.replace(CommonConstant.WROD_FILE, CommonConstant.PDF_FILE));
			topicName = exportVO.getTopicName();
		}
		if (ids.size() > 1) {
			log.info("走的压缩包");
			ExportPdfUtils.writePdfZip(pdfName, response, topicName + DateUtils.getCurrentHHmmssString("yyyy-MM-dd HH:mm:ss") + CommonConstant.ZIP_FILE);

		} else {

			ExportPdfUtils.downloadLocal(pdfName.get(0), response);

		}
		for (String s1 : pdfName) {
			if (new File(s1).exists()) {
				FileUtils.delete(new File(s1));
				//FileUtils.delete(new File(s1.replace(CommonConstant.PDF_FILE, CommonConstant.WROD_FILE)));
			}

		}
		log.info("结束");

	}

	/**
	 * 设置预测范围
	 * @param special
	 * @param singleId
	 * @param exportAiListVO
	 */
	private void setRang(String quantitativeIndicators,Special special, Long singleId, ExportAiListVO exportAiListVO, Map<Long, Long> categorys) {
		if(ObjectUtils.isNotEmpty(categorys.get(singleId))){
			String rangOut = singleSlideMapper.getRangOut(quantitativeIndicators,categorys.get(singleId), special.getSpecialId(), special.getControlGroup());
			exportAiListVO.setForecastRange(rangOut);
		}

	}




	@Override
	public R algorithm(AlgorithmIn req) {
		log.info("ai算法预测接口开始：");
		Long organizationId  = SecurityUtils.getLoginUser().getSysUser().getOrganizationId();
		//Long organizationId  = 1L;
		Long specialId = req.getSpecialId();
		MatrixReviewListIn mrl = new MatrixReviewListIn();
		mrl.setSpecialId(specialId);
		//0未预测、1预测成功、2预测失败、3预测中
		//		mrl.setForecastStatus("0");
		List<String> forecastStatusList = new ArrayList<>(Arrays.asList("0", "2"));
		mrl.setForecastStatusList(forecastStatusList);
		
		List<Integer> aiStatusFineList = new ArrayList<>(Arrays.asList(0, 2));
		mrl.setAiStatusFineList(aiStatusFineList);

		//2024.05.28新增合并标签查询过滤
		QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("organization_id", organizationId);
		queryWrapper.eq("del_flag", 1);
		//2024.06.14 算法是否支持(0:支持，1:不支持)
		queryWrapper.or().eq("algorithm_support_status", 1);
		List<Category> categoryList =  categoryService.list(queryWrapper);
		
		List<Long> categoryIdList = categoryList.stream().map(Category::getCategoryId).collect(Collectors.toList());
		mrl.setCategoryIdList(categoryIdList);
		List<MatrixReviewListOut> singleSlideList = slideMapper.getMatrixReview(mrl);
		if (CollectionUtils.isEmpty(singleSlideList)) {
			return R.ok();
		}

		//请求算法处理
		if(CollectionUtils.isNotEmpty(singleSlideList)){
			//修改算法执行时间
			List<Long> collect = singleSlideList.stream().map(MatrixReviewListOut::getSingleId).collect(Collectors.toList());
			LambdaQueryWrapper<SingleSlide> wrapper = new LambdaQueryWrapper<>();
			wrapper.in(SingleSlide::getSingleId,collect);
			SingleSlide singleSlide = new SingleSlide();
			singleSlide.setStartTime(new Date());
			singleSlideMapper.update(singleSlide,wrapper);
			for(MatrixReviewListOut matrixReviewListOut:singleSlideList){
				Long singleId = matrixReviewListOut.getSingleId();
				Long slideId = matrixReviewListOut.getSlideId();
				String imageUrl = matrixReviewListOut.getImageUrl();
				Long categoryId = matrixReviewListOut.getCategoryId();
				String organName = matrixReviewListOut.getOrganName();
				String aiImageUrl = matrixReviewListOut.getImageUrl();
				Long imageId = matrixReviewListOut.getImageId();
				String organizatinName = geNumber(organizationId);
				if(null != slideId && StringUtils.isNotEmpty(imageUrl)){
					if(imageUrl.toLowerCase().endsWith("svs") && !organName.equals("盲肠-回肠-直肠-结肠")){
						//请求算法接口
						try {
							//判断当前数据精细轮廓是否预测成功，如果成功的话，不需要修改预测状态
							SingleSlide hisSingleslide = singleSlideMapper.selectById(singleId);
							if(null != hisSingleslide.getAiStatusFine() && hisSingleslide.getAiStatusFine() != 1){
								//修改当前slide分析状态为进行中
								SingleSlide slide = new SingleSlide();
								slide.setSingleId(singleId);
								//精轮廓 0未预测、1预测成功、2预测失败、3预测中
								slide.setAiStatusFine(3);
								singleSlideMapper.updateById(slide);
							}
							log.info("AI算法请求内容是singleId:{},slideId:{},organizationId:{},imageUrl:{},algorithm_name:{}", singleId,slideId,organizationId,imageUrl,CommonConstant.ALGORITHM_MODEL_NAME);
							AiAlgorithm aiAlgorithm = new AiAlgorithm(singleId, slideId, categoryId, aiImageUrl, imageId);
							//BeanUtils.copyProperties(matrixReviewListOut, aiAlgorithm);
							aiAlgorithm.setAlgorithm_name(CommonConstant.ALGORITHM_MODEL_NAME);
							aiAlgorithm.setOrganizationName(organizatinName);
							aiAlgorithm.setOrganizationId(organizationId);
							aiAlgorithm.setSpecialId(specialId);
							log.info("AI算法请求完整数据{}", JSONUtil.toJsonStr(aiAlgorithm));
							String body = pythonService.algorithm(aiAlgorithm);
							JSONObject jsonObject = new JSONObject(body);
							Integer code = jsonObject.getInt("code");
							log.info("AI算法请求完整数据:{},AI算法请求返回数据:{},code:{}", JSONUtil.toJsonStr(aiAlgorithm), JSONUtil.toJsonStr(body),code);
							List<Integer> statusList = new ArrayList<>();
							statusList.add(200);//成功的
							//statusList.add(40001);//算法接口不存在
							if(code.equals(40001)){
								//修改当前slide分析状态为预测成功
								SingleSlide slideVo = new SingleSlide();
								slideVo.setSingleId(singleId);
								//0未预测、1预测成功、2预测失败、3预测中
								slideVo.setForecastStatus("1");
								slideVo.setAiStatusFine(1);
								singleSlideMapper.updateById(slideVo);
							}else{
								if (!statusList.contains(code)) {
									//修改当前slide分析状态为进行中
									SingleSlide slideVo = new SingleSlide();
									slideVo.setSingleId(singleId);
									//0未预测、1预测成功、2预测失败、3预测中
									slideVo.setForecastStatus("2");
									singleSlideMapper.updateById(slideVo);
									log.info("AI算法请求失败，状态列表：{},修改状态信息：{}", statusList.toString(),JSONUtil.toJsonStr(slideVo));
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							log.error("AI算法请求失败,切片id是{}",slideId);
						}finally {

						}
					}else{
						log.error("AI算法请求失败,切片id是{},图片非svs格式",slideId);
					}
				}
			}
		}
		return R.ok();
	}

	public  String geNumber(Long organizationId) {
		NumberFormat formatter = NumberFormat.getNumberInstance();
		formatter.setMinimumIntegerDigits(3);
		formatter.setGroupingUsed(false);
		return "C" + formatter.format(organizationId);
	}

	/**
	 * 设置参考范围
	 * @param special
	 * @param singleId
	 * @param exportAiListVO
	 * @param categorys
	 * @param genderFlag
	 */
	private void setReferenceScope(Special special, Long singleId, ExportAiListVO exportAiListVO, Map<Long, Long> categorys, String genderFlag) {
		List<BigDecimal> dataList= singleSlideMapper.getReferenceScope(exportAiListVO.getQuantitativeIndicators(),categorys.get(singleId), special.getSpecialId(),
				special.getControlGroup(),genderFlag,CommonConstant.NUMBER_0);
		if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(dataList)){
			BigDecimal bigDecimal = MathUtils.calculateAve(dataList.toArray(new BigDecimal[dataList.size()]), 3);
			log.info("平均值"+ bigDecimal);
			BigDecimal variance = MathUtils.variance(dataList.toArray(new BigDecimal[dataList.size()]), 3);
			log.info("总体方差" + variance);
			BigDecimal sqrt = MathUtils.sqrt(variance, 3);
			log.info("总体标准差" + sqrt);
			//平均值-标准差
			//BigDecimal subtract = bigDecimal.subtract(sqrt).setScale(3, RoundingMode.UP);
			//平均值+标准差
			//BigDecimal add = bigDecimal.add(sqrt).setScale(3, RoundingMode.UP);
			exportAiListVO.setAverageValue(bigDecimal+"±"+sqrt);

			//正态分布(下限)
			BigDecimal subtract2 = bigDecimal.subtract(new BigDecimal(1.96).multiply(sqrt)).setScale(3, RoundingMode.UP);
			if(subtract2.compareTo(BigDecimal.ZERO)<0){
				subtract2=BigDecimal.ZERO.setScale(3);
			}
			//正态分布(上限)
			BigDecimal add2 = bigDecimal.add(new BigDecimal(1.96).multiply(sqrt)).setScale(3, RoundingMode.UP);
			exportAiListVO.setNormalDistribution(subtract2+"-"+add2);
		}



	}

}
