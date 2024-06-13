package cn.staitech.fr.service.strategy.json;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.JsonFileService;
import cn.staitech.fr.service.JsonTaskService;
import cn.staitech.fr.service.SingleSlideService;
import cn.staitech.fr.service.SlideService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author: wangfeng
 * @create: 2024-05-10 15:52:39
 * @Description:
 */
@Service
@Slf4j
public class JsonTaskParserService {

	//    public static final ExecutorService jsonTaskExecutorService = new ThreadPoolExecutor(
	//            Runtime.getRuntime().availableProcessors() - 1,
	//            Runtime.getRuntime().availableProcessors() * 2,
	//            // 空闲线程等待工作的超时时间
	//            10L,
	//            TimeUnit.SECONDS,
	//            new LinkedBlockingQueue<Runnable>(4096),
	//            new ThreadFactory() {
	//                public Thread newThread(Runnable r) {
	//                    return new Thread(r, "json-task-service-thread-" + r.hashCode());
	//                }
	//            },
	//            new ThreadPoolExecutor.DiscardOldestPolicy());

	@Resource
	public SpecialAnnotationRelMapper specialAnnotationRelMapper;
	@Resource
	JsonTaskService jsonTaskService;
	@Resource
	JsonFileService jsonFileService;
	@Resource
	SlideService slideService;
	@Resource
	ParserStrategyFactory parserStrategyFactory;
	@Resource
	private List<CustomParserStrategy> customParserStrategies;
	@Resource
	private AnnotationMapper annotationMapper;

	@Resource
	private SingleSlideService singleSlideService;

	@Resource
	private CommonJsonParser commonJsonParser;


	@Autowired
	private AiForecastService aiForecastService;

	public void input(String input) {
		JSONObject jsonObject = JSON.parseObject(input);

		// 空数据返回
		if (jsonObject == null || !jsonObject.containsKey("algorithmCode")) {
			return;
		}

		String algorithmCode = jsonObject.get("algorithmCode").toString().trim();
		log.info("algorithmCode:{}", algorithmCode);

		// 解析任务元数据,并存入MySQL
		JsonTask jsonTask = parseJasonTask(jsonObject);
		if (jsonTask == null) {
			return;
		}

		if (jsonTask.getCode().equals("500") || StringUtils.isEmpty(algorithmCode) ) {
			SingleSlide singleSlide = new SingleSlide();
			singleSlide.setSingleId(jsonTask.getSingleId());
			//0未预测、1预测成功、2预测失败、3预测中
			singleSlide.setForecastStatus("2");
			singleSlideService.updateById(singleSlide);
			return;
		}

		log.info("jsonTask:{}", jsonTask);


		// 解析文件路径，并存入MySQL
		List<JsonFile> jsonFileList = parseJsonFileList(jsonTask);
		log.info("jsonFileList:{}", jsonFileList);
		int count = jsonFileList.size();
		if (count == 0) {
			return;
		}

		// 获取解析器
		ParserStrategy parser = parserStrategyFactory.getParserStrategy(algorithmCode);
		log.info("++++parser1:{}", parser);
		if (parser == null) {
			for (CustomParserStrategy parserStrategy : customParserStrategies) {
				if (parserStrategy.getAlgorithmCode().equals(algorithmCode)) {
					parser = parserStrategy;
				}
			}
		}

		if (null == parser ) {
			SingleSlide singleSlide = new SingleSlide();
			singleSlide.setSingleId(jsonTask.getSingleId());
			//0未预测、1预测成功、2预测失败、3预测中
			singleSlide.setForecastStatus("2");
			singleSlideService.updateById(singleSlide);
			log.info("AI预测切片id:{},算法名称标识:{},当前标签无法解析", jsonTask.getSingleId(),jsonTask.getAlgorithmCode());
			return;
		}
		
		log.info("+++parser2:{}", parser);
		//判断json数据目录是否存在
		for (JsonFile jsonFile : jsonFileList) {
			String fileUrl = jsonFile.getFileUrl();
			File file = new File(fileUrl);
			if (!file.exists()) {
				log.info("AI预测切片id:{},算法名称标识:{},目录不存在，地址{}", jsonTask.getSingleId(),jsonTask.getAlgorithmCode(),fileUrl);
				SingleSlide singleSlide = new SingleSlide();
				singleSlide.setSingleId(jsonTask.getSingleId());
				//0未预测、1预测成功、2预测失败、3预测中
				singleSlide.setForecastStatus("2");
				singleSlideService.updateById(singleSlide);
				return;
			}
		}
		Annotation annotation = getAnnotation(jsonTask);
		annotationMapper.deleteAiAnnotation(annotation);

		// 修改任务状态
		jsonTask.setStatus(1);
		jsonTaskService.updateById(jsonTask);

		try{
			for (JsonFile jsonFile : jsonFileList) {
				ParserStrategy finalParser = parser;

				log.info("Json文件解析开始:{} {} {} {}",System.currentTimeMillis() , jsonTask.getTaskId(), jsonFile.getFileUrl(), finalParser.getClass().getName());
				jsonFile.setStartTime(new Date());
				jsonFileService.updateById(jsonFile);

				finalParser.parseJson(jsonTask, jsonFile);
				// commonJsonParser.parseJson(jsonTask, jsonFile);

				log.info("Json文件解析结束:{} {} {} {}",System.currentTimeMillis() , jsonTask.getTaskId(), jsonFile.getFileUrl(), finalParser.getClass().getName());
				jsonFile.setEndTime(new Date());
				jsonFileService.updateById(jsonFile);
			}

			//        annotation.setContour("1");
			//        try {
			//            annotationMapper.deleteAiAnnotation(annotation);
			//        } catch (Exception e) {
			//            log.error("deleteAiAnnotation error:------------------------------------------------------4");
			//            log.error("deleteAiAnnotation error:{}", e.getMessage());
			//        }
			log.info("------------------------------------------------------5");
			//删除原有指标
			LambdaQueryWrapper<AiForecast> wrapper = new LambdaQueryWrapper<>();
			wrapper.eq(AiForecast::getSingleSlideId,jsonTask.getSingleId());
			aiForecastService.remove(wrapper);
			// 指标计算
			parser.alculationIndicators(jsonTask);
			log.info("------------------------------------------------------6");
		}catch(Exception e){
			log.info("------------------------------------------------------7");
			e.printStackTrace();
		}
		// 修改任务状态
		jsonTask.setStatus(2);
		jsonTaskService.updateById(jsonTask);

		SingleSlide singleSlide = new SingleSlide();
		singleSlide.setSingleId(jsonTask.getSingleId());
		//0未预测、1预测成功、2预测失败、3预测中
		singleSlide.setForecastStatus("1");
		singleSlide.setStructureTime(jsonTask.getStructureTime());
		singleSlideService.updateById(singleSlide);
		//        CountDownLatch countDownLatch = new CountDownLatch(count);
		//
		//        AtomicInteger id = new AtomicInteger();
		//        // 线程池 异步  调用策略提交任务
		//        for (JsonFile jsonFile : jsonFileList) {
		//            try {
		//                ParserStrategy finalParser = parser;
		//                jsonTaskExecutorService.submit(() -> {
		//                            log.info("---> {} {}", id.getAndIncrement(), jsonFile.getFileUrl());
		//                            // finalParser.parseJson(jsonTask, jsonFile);
		//                            finalParser.parseJson(jsonTask, jsonFile, finalParser);
		//                        }
		//                );
		//            } catch (Exception e) {
		//
		//            } finally {
		//                countDownLatch.countDown();
		//            }
		//        }
		//
		//        // 避免主线程无法执行到
		//        try {
		//            countDownLatch.await();
		//            // 指标计算
		//            parser.alculationIndicators(jsonTask);
		//            // 修改任务状态
		//            jsonTask.setStatus(1);
		//            jsonTaskService.updateById(jsonTask);
		//        } catch (InterruptedException e) {
		//
		//        }
	}

	private Annotation getAnnotation(JsonTask jsonTask) {
		Long sequenceNumber = commonJsonParser.getSequenceNumber(jsonTask.getSpecialId());

		Annotation annotation = new Annotation();
		annotation.setSequenceNumber(sequenceNumber);
		annotation.setSingleSlideId(jsonTask.getSingleId());
		return annotation;
	}

	/**
	 * 解析任务元数据,并存入MySQL
	 *
	 * @param jsonObject
	 * @return
	 */
	private JsonTask parseJasonTask(JSONObject jsonObject) {
		try {
			JsonTask jsonTask = new JsonTask();
			jsonTask.setAlgorithmCode(jsonObject.get("algorithmCode").toString());

			Long slideId = jsonObject.containsKey("slideId") ? Long.parseLong(jsonObject.get("slideId").toString()) : 0L;
			jsonTask.setSlideId(slideId);
			jsonTask.setImageId(jsonObject.containsKey("imageId") ? Long.parseLong(jsonObject.get("imageId").toString()) : 0L);
			jsonTask.setSingleId(jsonObject.containsKey("singleId") ? Long.parseLong(jsonObject.get("singleId").toString()) : 0L);
			jsonTask.setOrganizationId(jsonObject.containsKey("organizationId") ? Long.parseLong(jsonObject.get("organizationId").toString()) : 0L);
			//算法结构化时间处理
			Long structureTime = 0L;
			if(jsonObject.containsKey("elapsed_time")){
				Object eTimeObj = jsonObject.get("elapsed_time");
				if(null != eTimeObj){
					String eTimeStr = (String) eTimeObj;
					if(StringUtils.isNotEmpty(eTimeStr)){
						structureTime = (long)(Double.parseDouble(eTimeStr));
					}
				}
			}
			jsonTask.setStructureTime(structureTime);
			jsonTask.setCode(jsonObject.containsKey("code") ? jsonObject.get("code").toString() : "");
			jsonTask.setMsg(jsonObject.containsKey("msg") ? jsonObject.get("msg").toString() : "");
			jsonTask.setData(jsonObject.containsKey("data") ? jsonObject.get("data").toString() : "");

			jsonTask.setCreateTime(new Date());
			jsonTask.setStartTime(new Date());
			jsonTask.setStatus(0);

			Slide slide = slideService.getById(slideId);
			jsonTask.setSpecialId(slide.getSpecialId());
			SingleSlide singleSlide = singleSlideService.getById(jsonTask.getSingleId());
			jsonTask.setCategoryId(singleSlide.getCategoryId());
			// TODO:判重、丢弃任务
			jsonTaskService.save(jsonTask);
			return jsonTask;
		} catch (Exception e) {
			log.info("json-task-parser-service解析任务元数据异常:{}", e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 解析文件路径，并存入MySQL
	 *
	 * @param task
	 * @return
	 */
	private List<JsonFile> parseJsonFileList(JsonTask task) {
		JSONArray jsonArray;
		List<JsonFile> list = new ArrayList<>();
		try {
			jsonArray = new JSONArray(task.getData());
			for (int i = 0; i < jsonArray.length(); i++) {
				org.json.JSONObject jsonObject = jsonArray.getJSONObject(i);
				JsonFile jsonFile = new JsonFile();

				jsonFile.setStructureName(jsonObject.has("structureName") ? jsonObject.getString("structureName") : "");
				if (jsonObject.has("fileUrl")) {
					String fileUrl = jsonObject.getString("fileUrl");
					if (fileUrl.toLowerCase().endsWith(".json")) {
						jsonFile.setFileUrl(fileUrl);
					} else {
						continue;
					}
				}

				jsonFile.setTaskId(task.getTaskId());
				jsonFile.setStatus(0);

				jsonFile.setCreateTime(new Date());
				jsonFile.setStartTime(new Date());

				list.add(jsonFile);
			}
			jsonFileService.saveBatch(list);
		} catch (JSONException e) {
			e.printStackTrace();
			log.error("解析文件处理失败:{},taskId是{},singleId是{}", e, task.getTaskId(), task.getSingleId());
		}
		return list;
	}
}
