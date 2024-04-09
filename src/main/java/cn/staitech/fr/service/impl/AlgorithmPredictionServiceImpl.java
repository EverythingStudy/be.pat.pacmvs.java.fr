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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.json.JSONUtil;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.redis.service.RedisService;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.config.MapConstant;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.domain.WaxBlockInfo;
import cn.staitech.fr.domain.in.AlgorithmAnnIn;
import cn.staitech.fr.domain.in.StartPredictionIn;
import cn.staitech.fr.domain.out.AlgorithmImageOut;
import cn.staitech.fr.feign.PythonService;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.SlideMapper;
import cn.staitech.fr.service.AlgorithmPredictionService;
import cn.staitech.fr.service.AnnotationService;
import cn.staitech.fr.service.CategoryService;
import cn.staitech.fr.service.SlideService;
import cn.staitech.fr.service.WaxBlockInfoService;
import cn.staitech.fr.service.WaxBlockNumberService;
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
	private AnnotationService annotationService;

	@Resource
	private WaxBlockInfoService waxBlockInfoService;

	@Resource
	private WaxBlockNumberService waxBlockNumberService;

	@Resource
	private RedisService redisService;

	@Resource
	private CategoryService categoryService;



	@Resource
	private PythonService pythonService;


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
		//Map<Long, String> slideMap =  new HashMap<>();
		if(type == 0){
			//待切图的所有切片
			Map<String,Object> queryMap = new HashMap<>();
			queryMap.put("specialId", specialId);
			queryMap.put("processFlag", 0);
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
			list = slideMapper.getAlgorithmImage(queryMap);
		}
		//请求算法处理
		if(CollectionUtils.isNotEmpty(list)){
			for(AlgorithmImageOut algorithmImageOut:list){  
				Long slideId = algorithmImageOut.getSlideId();
				String imageUrl = algorithmImageOut.getImageUrl();
				Long imageId = algorithmImageOut.getImageId();
				if(null != slideId && StringUtils.isNotEmpty(imageUrl)){
					//{"slideId":10,"modelName":"脏器识别算法","imageUrl":"C:/Users/86153/Desktop/医疗PD/0320/2_shaowei/ST20Rf-AO-HE-LU-320-1-000020.svs"}
					Map<String,Object> dataMap = new HashMap<>();
					dataMap.put("imageId", imageId);
					dataMap.put("slideId", slideId);
					dataMap.put("organizationId", organizationId);
					dataMap.put("imageUrl", imageUrl);
					dataMap.put("algorithm_name", CommonConstant.RECOGNITION_MODEL_NAME);
					//请求算法接口
					try {
						log.info("AI算法请求内容是imageId:{},slideId:{},organizationId:{},imageUrl:{},algorithm_name:{}", imageId,slideId,organizationId,imageUrl,CommonConstant.RECOGNITION_MODEL_NAME);
						StartRecognition startRecognition = new StartRecognition();
						startRecognition.setImageId(imageId);
						startRecognition.setSlideId(slideId);
						startRecognition.setImageUrl(imageUrl);
						startRecognition.setAlgorithm_name(CommonConstant.RECOGNITION_MODEL_NAME);
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


	public void process(AlgorithmAnnIn  algorithmAnnIn) throws Exception {
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
				Long organizationId = SecurityUtils.getLoginUser().getSysUser().getOrganizationId();
//		Long organizationId = 1L;
		//专题id
		Long specialId = slide.getSpecialId();
		Long slideId = slide.getSlideId();
		//处理状态（0：待切图,1：切图中,2：已切图 3：切图失败）
		int processFlag = slide.getProcessFlag();
		if(processFlag == 2){
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
			if(CollectionUtils.isEmpty(waxinfoList)){
				 waxinfoList = waxBlockInfoService.getWaxBlockInfoList(slideId, waxCode,"");
			}
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
		}

		if(null != waxDataMap && !waxDataMap.isEmpty()){
			//查询所属切片AI脏器信息
			Map<String,Integer> slideCountMap = new HashMap<String, Integer>();
			int slideCountSize = 0;

			List<AnnotationCountByCategory>  countList = annotationMapper.getCategoryCount(slideId);
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
				//核对状态 0：初始 1：正确 2：错误 3：修正正常
				int checkStatus = 0;
				if(waxDataMapSize != slideCountSize){
					checkStatus = 2;
				}else{
					//遍历蜡块==》
					boolean slideTag = true;
					for(Map.Entry<String, Integer> entry:waxDataMap.entrySet()){
						String organName = entry.getKey();
						Integer organNumber = entry.getValue();
						boolean tag = containsOrgan(organName, organNumber, slideCountMap);
						if(!tag){
							slideTag = false;
							checkStatus = 2;
							break;
						}
					}
					//全部匹配成功
					if(slideTag){
						checkStatus = 1;
					}
					//更新checkStatus
					Slide updateSlide = new Slide();
					updateSlide.setSlideId(slideId);
					updateSlide.setCheckStatus(checkStatus);
					updateSlide.setCheckBy(0l);
					updateSlide.setCheckTime(new Date());
					//					annotationMapper.updateById(updateSlide);
					slideMapper.updateById(updateSlide);
				}
			}
		}
		//检查当前动物号所属的所有切片是否正常
		Slide oldSlide = slideService.getById(slideId);
		//动物号
		String animalCode = oldSlide.getAnimalCode();

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


}
