package cn.staitech.fr.service.impl;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.json.JSONUtil;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.redis.service.RedisService;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.config.MapConstant;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.Category;
import cn.staitech.fr.domain.Image;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.domain.Special;
import cn.staitech.fr.domain.WaxBlockInfo;
import cn.staitech.fr.domain.in.AlgorithmAnnIn;
import cn.staitech.fr.domain.in.StartPredictionIn;
import cn.staitech.fr.domain.out.AlgorithmImageOut;
import cn.staitech.fr.feign.PythonOrganRecognitionService;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.ImageMapper;
import cn.staitech.fr.mapper.SlideMapper;
import cn.staitech.fr.mapper.SpecialMapper;
import cn.staitech.fr.service.AlgorithmPredictionService;
import cn.staitech.fr.service.CategoryService;
import cn.staitech.fr.service.SlideService;
import cn.staitech.fr.service.WaxBlockInfoService;
import cn.staitech.fr.vo.annotation.AnnotationCountByCategory;
import cn.staitech.fr.vo.annotation.StartRecognition;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wanglibei
 * @version V1.0
 * @ClassName: AlgorithmPredictionServiceImpl
 * @Description:服务实现类
 * @date 2023年11月2日
 */
@Service
@Slf4j
public class AlgorithmPredictionServiceImpl implements AlgorithmPredictionService {

	private static final ExecutorService EXECUTOR = ExecutorBuilder.create().setCorePoolSize(Runtime.getRuntime().availableProcessors()).setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2).setKeepAliveTime(0).setWorkQueue(new LinkedBlockingQueue<Runnable>(4096)).build();


	@Resource
	private SlideService slideService;

	@Resource
	private SlideMapper slideMapper;

	@Resource
	private AnnotationMapper annotationMapper;

	@Resource
	private WaxBlockInfoService waxBlockInfoService;

	@Resource
	private RedisService redisService;

	@Resource
	private SpecialMapper specialMapper;
	
	@Resource
	private ImageMapper imageMapper;

	@Resource
	private PythonOrganRecognitionService pythonService;
	
	@Resource
    private CategoryService categoryService;


	@SuppressWarnings("rawtypes")
	@Override
	public R startPrediction(StartPredictionIn req) {
		Long organizationId  = SecurityUtils.getLoginUser().getSysUser().getOrganizationId();
		Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
		Long specialId = req.getSpecialId();
		//启动切片方式 0：全部启动 1：部分启动
		int type = req.getType();
		List<Long> slideIdList = req.getSlideIdList();
		List<AlgorithmImageOut> list = new ArrayList<>();
		if(type == 0){
			//待切图的所有切片
			Map<String,Object> queryMap = new HashMap<>();
			queryMap.put("specialId", specialId);
			queryMap.put("processFlag", 0);
			//切片名称解析，0：成功；1：失败
			queryMap.put("analyzeStatus", 0);
			list = slideMapper.getAlgorithmImage(queryMap);
		}else{
			if(CollectionUtils.isEmpty(slideIdList)) {
				return R.ok();
			}
			//查询相关切片数据
			Map<String,Object> queryMap = new HashMap<>();
			queryMap.put("specialId", specialId);
			queryMap.put("processFlag", 0);
			queryMap.put("list", slideIdList);
			//切片名称解析，0：成功；1：失败
			queryMap.put("analyzeStatus", 0);
			list = slideMapper.getAlgorithmImage(queryMap);
		}
		//请求算法处理
		if(CollectionUtils.isNotEmpty(list)){
			for(AlgorithmImageOut algorithmImageOut:list){  
				Long slideId = algorithmImageOut.getSlideId();
				String imageUrl = algorithmImageOut.getImageUrl();
				Long imageId = algorithmImageOut.getImageId();
				String organizatinName = geNumber(organizationId);
				if(null != slideId && StringUtils.isNotEmpty(imageUrl)){
					if(imageUrl.toLowerCase().endsWith("svs")){
						//请求算法接口
						try {
							log.info("AI算法请求内容是imageId:{},slideId:{},organizationId:{},imageUrl:{},algorithm_name:{}", imageId,slideId,organizationId,imageUrl,CommonConstant.RECOGNITION_MODEL_NAME);
							StartRecognition startRecognition = new StartRecognition();
							startRecognition.setImageId(imageId);
							startRecognition.setSlideId(slideId);
							startRecognition.setImageUrl(imageUrl);
							startRecognition.setAlgorithm_name(CommonConstant.RECOGNITION_MODEL_NAME);
							startRecognition.setOrganizationName(organizatinName);
							startRecognition.setOrganizationId(organizationId);
							String body = pythonService.startPrediction(startRecognition);
							log.info("AI算法请求返回数据{}", JSONUtil.toJsonStr(body));
							JSONObject jsonObject = new JSONObject(body);
							Integer code = jsonObject.getInt("code");
							if (code.equals(200)) {
								//修改当前slide分析状态为进行中
								Slide slide = new Slide();
								slide.setSlideId(slideId);
								//处理状态（0：待切图,1：切图中,2：已切图 3：切图失败）
								slide.setProcessFlag(1);
								slide.setInitiateBy(userId);
								slide.setInitiateTime(new Date());
								slideService.updateById(slide);
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


	@Override
	public void recognition(AlgorithmAnnIn algorithmAnnIn) {
		EXECUTOR.submit(new RecognitionThread(algorithmAnnIn));
	}


	class RecognitionThread implements Runnable {
		// type 1:标注保存  2：标注修改
		private final AlgorithmAnnIn  algorithmAnnIn;

		public RecognitionThread(AlgorithmAnnIn  algorithmAnnIn) {
			this.algorithmAnnIn = algorithmAnnIn;
		}

		@Override
		public void run() {
			try {
				process(this.algorithmAnnIn);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	public void process(AlgorithmAnnIn  algorithmAnnIn){
		Long startTime = System.currentTimeMillis();
		Long slideId = algorithmAnnIn.getSlideId();
		if(null != slideId){
			// 查询切片列表  
			QueryWrapper<Slide> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("slide_id", slideId);
			List<Slide> slideList = slideMapper.selectList(queryWrapper);
			//			List<Slide> slideList = slideService.list(queryWrapper);
			//1、拆分核对操作
			if(CollectionUtils.isNotEmpty(slideList)){
				//直接遍历fileList即可
				for (Slide slide : slideList) {
					checkSlide(slide);
				}
			}
			log.info("AsyncSave-Time-Consuming : {} ms", System.currentTimeMillis() - startTime);
		}
	}


	public void checkSlide(Slide slide) {
		//启动时间
		Date initiateTime =slide.getInitiateTime();
		Date currentTime = DateUtil.date();
		if(null != initiateTime){
			Image image = imageMapper.selectById(slide.getImageId());
			// Calculate the difference in milliseconds
			Long diffInMilliseconds = currentTime.getTime() - initiateTime.getTime();
			// Convert the difference to seconds
			double diffInSeconds = diffInMilliseconds.doubleValue() / 1000L;
			String imageSize = "";
			if(StringUtils.isNotEmpty(image.getSize())){
				imageSize = String.format("%.2f", Long.parseLong(image.getSize()) / (1024.0 * 1024.0)) + "MB";
			}
			log.info("AI统脏器识别算法耗时统计,slideId:{},imageId:{},切片名称：{},切片大小：{},开始时间：{},结束时间：{},总共历时：{}秒", slide.getSlideId(),slide.getImageId(),image.getImageName(),imageSize,DateUtil.format(slide.getInitiateTime(), "yyyy-MM-dd HH:mm:ss") ,DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"),diffInSeconds);
		}
		//切片名称解析，0：成功；1：失败
		String analyzeStatus = slide.getAnalyzeStatus();
		if(StringUtils.isNotEmpty(analyzeStatus) && analyzeStatus.equals("1")){
			//核对的动物号为空，直接对比错误
			Slide updateSlide = new Slide();
			//核对状态 0：初始 1：正确 2：修正正常 3：错误
			updateSlide.setCheckStatus(3);
			updateSlide.setCheckBy(0l);
			updateSlide.setCheckTime(new Date());
			updateSlide.setAnimalCheckStatus(3);
			slideMapper.updateById(updateSlide);
			return ;
		}
		//专题id
		Long specialId = slide.getSpecialId();
		Long slideId = slide.getSlideId();
		Slide slideInfo = slideMapper.selectById(slideId);
		Special special = specialMapper.selectById(slideInfo.getSpecialId());
		Long organizationId = special.getOrganizationId();

		//处理状态（0：待切图,1：切图中,2：已切图 3：切图失败）
		int processFlag = slide.getProcessFlag();
		if(processFlag == 3){
			//更新checkStatus
			Slide updateSlide = new Slide();
			updateSlide.setSlideId(slideId);
			//核对状态 0：初始 1：正确 2：修正正常 3：错误
			updateSlide.setCheckStatus(3);
			updateSlide.setCheckBy(0l);
			updateSlide.setCheckTime(new Date());
			updateSlide.setAnimalCheckStatus(3);
			slideMapper.updateById(updateSlide);
			return ;
		}
		//蜡块编号
		String waxCode = slide.getWaxCode();
		// 性别（M:雄；F:雌）
		String genderFlag = slide.getGenderFlag();

		Map<String,Integer> waxDataMap = new HashMap<String, Integer>();
		int waxDataMapSize = 0;
		String cacheKey = "";
		if(StringUtils.isNotEmpty(genderFlag)){
			cacheKey = CommonConstant.WAX_BLOCK_INFO+specialId+"_"+waxCode+"_"+genderFlag;
		}else{
			cacheKey = CommonConstant.WAX_BLOCK_INFO+specialId+"_"+waxCode;
		}
		waxDataMap = redisService.getCacheObject(cacheKey);

		if (null == waxDataMap || waxDataMap.isEmpty()) {
			waxDataMap = new HashMap<String, Integer>();
			//查询所属蜡块完整信息
			List<WaxBlockInfo> waxinfoList = waxBlockInfoService.getWaxBlockInfoList(slideId, waxCode,genderFlag);
			//处理蜡块信息
			if(CollectionUtils.isNotEmpty(waxinfoList)){
				for(WaxBlockInfo info:waxinfoList){
					String organName = info.getOrganName();
					Integer organNumber = info.getOrganNumber();
					waxDataMap.put(organName, organNumber);
				}
				waxDataMapSize = waxDataMap.size();
				redisService.setCacheObject(cacheKey,waxDataMap, 24L, TimeUnit.HOURS);
			}
		}else{
			waxDataMapSize = waxDataMap.size();
		}

		if(null != waxDataMap && !waxDataMap.isEmpty()){
			//查询所属切片AI脏器信息
			Map<String,Integer> slideCountMap = new HashMap<String, Integer>();
			int slideCountSize = 0;
			Annotation annotationParm = new Annotation();
			annotationParm.setSlideId(slideId);
			//查询
			QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("del_flag", 1);
			List<Category> categoryList =  categoryService.list(queryWrapper);
			List<Long> categoryIdList = categoryList.stream().map(Category::getCategoryId).collect(Collectors.toList());
			annotationParm.setCategoryIdList(categoryIdList);
			List<AnnotationCountByCategory>  countList = annotationMapper.getCategoryCount(annotationParm);
			if(CollectionUtils.isNotEmpty(countList)){
				for(AnnotationCountByCategory annotationCount:countList){
					Long categoryId = annotationCount.getCategoryId();
					int categoryCount = annotationCount.getTotalCount();
					//标签id转为名称
					String oCategory = organizationId.toString()+categoryId.toString();
					String categoryFullName = MapConstant.getCategory(oCategory);
					if(StringUtils.isNotEmpty(categoryFullName)){
						slideCountMap.put(categoryFullName, categoryCount);
					}
				}
				slideCountSize = slideCountMap.size();


				//对比处理 
				//核对状态 0：初始 1：正确 2：修正正常 3：错误
				int checkStatus = 0;
				if(waxDataMapSize != slideCountSize){
					checkStatus = 3;
				}else{
					//遍历蜡块==》
					boolean slideTag = true;
					for(Map.Entry<String, Integer> entry:waxDataMap.entrySet()){
						String organName = entry.getKey();
						Integer organNumber = entry.getValue();
						boolean tag = containsOrgan(organName, organNumber, slideCountMap);
						if(!tag){
							slideTag = false;
							checkStatus = 3;
							break;
						}
					}
					//全部匹配成功
					if(slideTag){
						checkStatus = 1;
					}
				}
				//更新checkStatus
				Slide updateSlide = new Slide();
				updateSlide.setSlideId(slideId);
				updateSlide.setCheckStatus(checkStatus);
				updateSlide.setCheckBy(0l);
				updateSlide.setCheckTime(new Date());
				slideMapper.updateById(updateSlide);
			}
		}else{
			//蜡块查询不到信息
			Slide updateSlide = new Slide();
			updateSlide.setSlideId(slideId);
			updateSlide.setCheckStatus(3);
			updateSlide.setCheckBy(0l);
			updateSlide.setCheckTime(new Date());
			slideMapper.updateById(updateSlide);
		}
		//检查当前动物号所属的所有切片是否正常
		String animalCode = slide.getAnimalCode();
		QueryWrapper<Slide> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("animal_code", animalCode);
		queryWrapper.eq("special_id", specialId);
		List<Slide> queryList = slideService.list(queryWrapper);
		// 按照checkStatus属性分组到Map<String, List<Slide>>
		Map<Integer, List<Slide>> mapByCheckStatus = queryList.stream().collect(Collectors.groupingBy(Slide::getCheckStatus));
		System.out.println(JSONUtil.toJsonStr(mapByCheckStatus));
		if(null !=mapByCheckStatus&& !mapByCheckStatus.isEmpty()){
			UpdateWrapper<Slide> updateWrapper = new UpdateWrapper<>();
			updateWrapper.eq("animal_code", animalCode);
			updateWrapper.eq("special_id", specialId);
			Slide sd = new Slide();
			//当前动物号检查状态 核对状态 0：初始 1：正确 2：修正正常 3：错误
			if(mapByCheckStatus.containsKey(3)){
				sd.setAnimalCheckStatus(3);
			}else{
				sd.setAnimalCheckStatus(1);
			}
			slideService.update(sd, updateWrapper);
		}

	}

	//模糊匹配，判断是否包括该脏器
	private boolean containsOrgan(String organName,int organNumber,Map<String,Integer> slideCountMap){
		boolean containsValue  = false;
		for(Map.Entry<String, Integer> entry:slideCountMap.entrySet()){
			String slideOrganName = entry.getKey();
			int slideOrganNumber = entry.getValue();
			//模糊匹配
			if (organName.contains(slideOrganName)) {
				//数量相等
				if (organNumber == slideOrganNumber) {
					containsValue = true;
					break;
				}
			}
		}
		return containsValue ;
	}

	public  String geNumber(Long organizationId) {
		NumberFormat formatter = NumberFormat.getNumberInstance();
		formatter.setMinimumIntegerDigits(3);
		formatter.setGroupingUsed(false);
		return "C" + formatter.format(organizationId);
	}


}
