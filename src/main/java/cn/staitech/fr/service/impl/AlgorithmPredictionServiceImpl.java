package cn.staitech.fr.service.impl;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import cn.hutool.json.JSONUtil;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.domain.in.StartPredictionIn;
import cn.staitech.fr.domain.out.AlgorithmImageOut;
import cn.staitech.fr.mapper.SlideMapper;
import cn.staitech.fr.service.AlgorithmPredictionService;
import cn.staitech.fr.service.SlideService;
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

	@Resource
	private SlideService slideService;

	@Resource
	private SlideMapper slideMapper;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${algorithmPredictionPath}")
	private String algorithmPredictionPath;

	public static String geNumber(Long organizationId) {
		NumberFormat formatter = NumberFormat.getNumberInstance();
		formatter.setMinimumIntegerDigits(3);
		formatter.setGroupingUsed(false);
		return "C" + formatter.format(organizationId);
	}


	@SuppressWarnings("rawtypes")
	@Override
	public R startPrediction(StartPredictionIn req) {
		Long specialId = req.getSpecialId();
		//启动切片方式 0：全部启动 1：部分启动
		int type = req.getType();
		List<Long> slideIdList = req.getSlideIdList();
		Map<Long, String> slideMap =  new HashMap<>();
		if(type == 0){
			//待切图的所有切片
			Map<String,Object> queryMap = new HashMap<>();
			queryMap.put("specialId", specialId);
			queryMap.put("processFlag", 0);
			List<AlgorithmImageOut> list = slideMapper.getAlgorithmImage(queryMap);
			if(CollectionUtils.isNotEmpty(list)){
				 slideMap = list.stream().collect(Collectors.toMap(AlgorithmImageOut::getSlideId, AlgorithmImageOut::getImageUrl));
			}
		}else{
			if(CollectionUtils.isEmpty(slideIdList)) {
				return R.ok();
			}
			//查询相关切片数据
			Map<String,Object> queryMap = new HashMap<>();
			queryMap.put("specialId", specialId);
			queryMap.put("processFlag", 0);
			queryMap.put("list", slideIdList);
			List<AlgorithmImageOut> list = slideMapper.getAlgorithmImage(queryMap);
			if(CollectionUtils.isNotEmpty(list)){
				 slideMap = list.stream().collect(Collectors.toMap(AlgorithmImageOut::getSlideId, AlgorithmImageOut::getImageUrl));
			}
		}
		//请求算法处理
		if(null != slideMap && !slideMap.isEmpty()){
			for(Map.Entry<Long, String> entry:slideMap.entrySet()){  
				Long slideId = entry.getKey();
				String imageUrl = entry.getValue();
				if(null != slideId && StringUtils.isNotEmpty(imageUrl)){
					//				{"slideId":10,"modelName":"脏器识别算法","imageUrl":"C:/Users/86153/Desktop/医疗PD/0320/2_shaowei/ST20Rf-AO-HE-LU-320-1-000020.svs"}
					Map<String,Object> dataMap = new HashMap<>();
					dataMap.put("slideId", slideId);
					dataMap.put("imageUrl", imageUrl);
					dataMap.put("modelName", CommonConstant.RECOGNITION_MODEL_NAME);
					//请求算法接口
					try {
						ResponseEntity<String> resp = restTemplate.postForEntity(algorithmPredictionPath, JSONUtil.toJsonStr(dataMap), String.class);
						String body = resp.getBody();
						log.info("AI算法请求返回数据{},内容是{}", JSONUtil.toJsonStr(resp), body);
						JSONObject jsonObject = new JSONObject(body);
						Integer code = jsonObject.getInt("code");
						if (code.equals(200)) {
							//修改当前slide分析状态为进行中
							Slide slide = new Slide();
							slide.setSlideId(slideId);
							//处理状态（0：待切图,1：切图中,2：已切图 3：切图失败）
							slide.setProcessFlag(1);
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
}
