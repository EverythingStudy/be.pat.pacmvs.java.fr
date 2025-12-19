package cn.staitech.fr.service.strategy.json.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.DynamicData;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.ProjectMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import cn.staitech.fr.utils.MathUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @author chenly
 * @version V1.0
 * @ClassName: ProstateGlandParserStrategyImpl
 * @Description:前列腺
 * @date 2025年9月12日
 */
@Slf4j
@Component("Prostate")
public class ProstateGlandParserStrategyImpl extends AbstractCustomParserStrategy {

    @Resource
    public SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private SingleSlideMapper singleSlideMapper;
    @Resource
    private AiForecastService aiForecastService;
    @Resource
    private CommonJsonParser commonJsonParser;
    @Resource
    private CommonJsonCheck commonJsonCheck;
    @Autowired
    private AreaUtils areaUtils;
    @Resource
    private ProjectMapper projectMapper;
    //默认对照组值
    private static final String DEFAULT_CONTROL_GROUP_VALUE = "1";

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.info("ProstateGlandParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("前列腺结构指标计算开始");
        // Project special = projectMapper.selectById(jsonTask.getSpecialId());
        // String controlGroup = StringUtils.isNotEmpty(special.getControlGroup()) ? special.getControlGroup() : DEFAULT_CONTROL_GROUP_VALUE;
        //Integer count = singleSlideMapper.getCategoryIdCountByGroupCode(jsonTask.getCategoryId(), jsonTask.getSingleId(), controlGroup);
        Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
        SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
        //a 腺泡面积（单个）+ c 腺泡周长（单个）
        List<Annotation> organAreaA = commonJsonParser.getStructureContourList(jsonTask, "12C06D");
        //b 腺泡面积（全片）
        BigDecimal organAreaB = commonJsonParser.getOrganArea(jsonTask, "12C06D").getStructureAreaNum();
        //F 组织轮廓面积
        BigDecimal organAreF = new BigDecimal(singleSlide.getArea()).setScale(3, RoundingMode.HALF_UP);
        indicatorResultsMap.put("腺泡面积（全片）", createIndicator(organAreaB.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "12C06D"));
        indicatorResultsMap.put("前列腺面积", createNameIndicator("Seminal vesicle area", organAreF.toString(), SQ_MM, "12C111"));
        indicatorResultsMap.put("腺泡面积占比", createNameIndicator("Acinar area%", commonJsonParser.getProportion(organAreaB, organAreF), PERCENTAGE, areaUtils.getStructureIds("12C06D", "12C111")));
        //腺上皮面积占比集合
        List<BigDecimal> epithelialList = new ArrayList<>();
        //腺腔面积占比集合
        List<BigDecimal> lumenList = new ArrayList<>();
        for (Annotation annotation : organAreaA) {
            BigDecimal structureAreaNum = annotation.getStructureAreaNum();
            Annotation contourInsideOrOutside = commonJsonParser.getContourInsideOrOutside(jsonTask, annotation.getContour(), "12C0E9", true);
            //D/A
            epithelialList.add(new BigDecimal(100).subtract(getProportion(contourInsideOrOutside.getStructureAreaNum(), structureAreaNum)));
            lumenList.add(getProportion(contourInsideOrOutside.getStructureAreaNum(), structureAreaNum));
        }
        indicatorResultsMap.put("腺上皮面积占比（单个）", createNameIndicator("Acinar epithelial area% (per)", MathUtils.getConfidenceInterval(epithelialList), PERCENTAGE, areaUtils.getStructureIds("12D074", "12C0E9")));
        indicatorResultsMap.put("腺腔面积占比（单个）", createNameIndicator("Acinar lumen area% (per)", MathUtils.getConfidenceInterval(lumenList), PERCENTAGE, areaUtils.getStructureIds("12D074", "12C0E9")));

        Annotation annotation1 = new Annotation();
        annotation1.setAreaName("腺泡面积（单个）");
        annotation1.setAreaUnit(SQ_UM_THOUSAND);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "12C06D", annotation1, 1);
        Annotation annotation2 = new Annotation();
        annotation2.setPerimeterName("腺泡周长（单个）");
        annotation2.setPerimeterUnit(MM);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "12C06D", annotation2, 3);
        Annotation annotation3 = new Annotation();
        annotation3.setAreaName("腺腔面积（单个）");
        annotation3.setAreaUnit(SQ_UM_THOUSAND);
        commonJsonParser.putAnnotationDynamicData(jsonTask, "12C06D", "12C0E9", annotation3, 1, true);

        aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
    }

    public void putAnnotationDynamicData(JsonTask jsonTask, Annotation annotation) {
        Long sequenceNumber = commonJsonParser.getSequenceNumber(jsonTask.getSpecialId());
        List<Annotation> annotationList = getStructureContourList(jsonTask, "12C0E9");

        if (CollectionUtil.isEmpty(annotationList)) {
            return;
        }

        // 批量处理数据
        List<Annotation> batchUpdates = new ArrayList<>(annotationList.size());

        // 预处理通用数据
        boolean hasAreaName = annotation.getAreaName() != null;

        String areaUnit = annotation.getAreaUnit();

        for (Annotation item : annotationList) {
            Annotation annotationBy = getContourInsideOrOutside(jsonTask, item.getContour(), "12C06D", true);

            if (annotationBy == null) {
                continue;
            }

            // 预处理动态数据
            JSONObject dynamicDataJson = new JSONObject();
            if (item.getDynamicDataList() != null) {
                dynamicDataJson = JSONObject.parseObject(item.getDynamicDataList().toString());
            }

            JSONArray jsonArray = dynamicDataJson.getJSONArray("dynamicData");
            Set<String> existingNames = new HashSet<>();
            if (jsonArray != null) {
                for (int j = 0; j < jsonArray.size(); j++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(j);
                    existingNames.add(jsonObject.getString("name"));
                }
            } else {
                jsonArray = new JSONArray();
            }

            // 处理面积数据
            if (hasAreaName) {
                DynamicData dynamicData = new DynamicData();
                dynamicData.setName(annotation.getAreaName());
                dynamicData.setData(commonJsonParser.convertToSquareMicrometer(String.valueOf(annotationBy.getStructureAreaNum().subtract(item.getStructureAreaNum()))));
                dynamicData.setUnit(areaUnit);
                if (existingNames.contains(dynamicData.getName())) {
                    // 更新现有数据
                    for (int j = 0; j < jsonArray.size(); j++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(j);
                        if (Objects.equals(jsonObject.getString("name"), dynamicData.getName())) {
                            jsonObject.put("data", dynamicData.getData());
                            break;
                        }
                    }
                } else {
                    // 添加新数据
                    jsonArray.add(dynamicData);
                }
            }

            // 更新注解对象
            if (jsonArray.size() > 0) {
                JSONObject resultJson = new JSONObject();
                resultJson.put("dynamicData", jsonArray);
                item.setSequenceNumber(sequenceNumber);
                item.setDynamicData(resultJson.toString());
                item.setSingleSlideId(jsonTask.getSingleId());
                batchUpdates.add(item);
            }
        }

        // 批量更新数据库
        if (!batchUpdates.isEmpty()) {
            commonJsonParser.batchUpdateAnnotations(batchUpdates);
        }
    }

    @Override
    public String getAlgorithmCode() {
        return "Prostate";
    }
}
