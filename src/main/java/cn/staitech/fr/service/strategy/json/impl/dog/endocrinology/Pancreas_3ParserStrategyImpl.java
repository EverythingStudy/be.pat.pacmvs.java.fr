package cn.staitech.fr.service.strategy.json.impl.dog.endocrinology;

import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @ClassName: Pancreas_3ParserStrategyImpl
 * @Description:犬-胰腺
 * @author wanglibei
 * @date 2026年2月11日
 * @version V1.0
 */
@Slf4j
@Service("Pancreas_3")
public class Pancreas_3ParserStrategyImpl extends AbstractCustomParserStrategy{
	@Resource
	private SingleSlideMapper singleSlideMapper;
	@Resource
	private AiForecastService aiForecastService;
	@Resource
	private CommonJsonParser commonJsonParser;
	@Resource
	private CommonJsonCheck commonJsonCheck;

	@PostConstruct
	public void init() {
		setCommonJsonParser(commonJsonParser);
		setCommonJsonCheck(commonJsonCheck);
		log.debug("Pancreas_3ParserStrategyImpl init");
	}


	@Override
	public void alculationIndicators(JsonTask jsonTask) {
		/**
		 * 
		 * 结构编码
			上皮细胞核 305075
			酶原颗粒 305076
			胰岛 305077
			胰岛细胞核 305078
			间质 305027
			导管 30506F
			血管 305003
			组织轮廓 305111
		 */
		/**
		 * 
		 * 算法输出指标指标代码（仅限本文档）单位（保留小数点后三位）备注相关结构
			上皮细胞核数量 A 个 305075
			酶原颗粒面积 B mm² 数据相加输出 305076
			胰岛数量 C 个 数据相加输出 305077
			胰岛面积（单个） D ×10³ μm2 单个胰岛面积输出显示在单个胰岛轮廓弹窗中，不显示在指标表格里 305077
			胰岛面积（全片）E mm²数据相加输出305077
			胰岛细胞核数量（单个）  F 个 单个胰岛内胰岛细胞核数量输出显示在单个胰岛轮廓弹窗中，不显示在指标表格里 305077、305078
			间质面积 G mm² 数据相加输出 305027
			导管面积 H mm² 数据相加输出 30506F
			血管面积 I mm² 数据相加输出 305003
			组织轮廓面积 J mm² 数据相加输出仅辅助指标10计算，数值不显示在页面指标表格里 305111
			胰岛细胞核数量（全片） K 个 数据相加输出 305077、305078
		 */
		Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
		//A 上皮细胞核数量
		Integer A_305075_count = getOrganAreaCount(jsonTask, "305075");
		//B 酶原颗粒面积
		BigDecimal B_305076_organArea = getOrganArea(jsonTask, "305076").getStructureAreaNum();
		//C 胰岛数量
		Integer C_305077_count = getOrganAreaCount(jsonTask, "305077");
		//E 胰岛面积（全片）mm²
		BigDecimal E_305077_organArea = getOrganArea(jsonTask, "305077").getStructureAreaNum();
		//F 胰岛细胞核数量（单个）305077、305078
//		Integer F_305078_count = getOrganAreaCount(jsonTask, "305078");
		//G 间质面积 305027
		BigDecimal G_305027_organArea = getOrganArea(jsonTask, "305027").getStructureAreaNum();
		//H 导管面积  30506F
		BigDecimal H_30506F_organArea = getOrganArea(jsonTask, "30506F").getStructureAreaNum();
		//I 血管面积 305003
		BigDecimal I_305003_organArea = getOrganArea(jsonTask, "305003").getStructureAreaNum();
		//J 组织轮廓面积
		SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
		BigDecimal J_organArea = new BigDecimal(singleSlide.getArea());
		// K 胰岛细胞核数量（全片）305077、305078
//		Integer  K_305078_count = getOrganAreaCount(jsonTask, "305078");

		//4 胰岛细胞核密度（单个）个/10³ μm2 4=F/D
		//        List<BigDecimal> dataList = new ArrayList<>();
		//        if (CollectionUtils.isNotEmpty(annotationList)) {
		//            for (Annotation annotation : annotationList) {
		//                String contour = annotation.getContour();
		//                //F 胰岛细胞核数量（单个）
		//                Annotation temp = getContourInsideOrOutside(jsonTask, contour, "305078", true);
		//                if (annotation.getStructureAreaNum().compareTo(BigDecimal.ZERO) > 0 && temp.getCount() != 0) {
		//                    dataList.add(BigDecimal.valueOf(temp.getCount()).divide((annotation.getStructureAreaNum().multiply(BigDecimal.valueOf(1000))), 3, RoundingMode.HALF_UP));
		//                }
		//            }
		//        }
		/**
		 * 
        //H 单位10³ μm2 导管面积（单个）;
        Integer count2 = getOrganAreaCount(jsonTask, "30506F");
        //M 血管内红细胞面积
        Annotation annotationInner = getInsideOrOutside(jsonTask, "305003", "305004", true);
        //N 血管外红细胞面积
        Annotation annotationOuter = getInsideOrOutside(jsonTask, "305003", "305004", false);
        //Q 红细胞面积 305004
        BigDecimal organAreaQ = getOrganArea(jsonTask, "305004").getStructureAreaNum();
		 */

		/**
		 * 
		 * 算法输出指标指标代码（仅限本文档）单位（保留小数点后三位）备注相关结构
			上皮细胞核数量 A 个 305075
			酶原颗粒面积 B mm² 数据相加输出 305076
			胰岛数量 C 个 数据相加输出 305077
			胰岛面积（单个） D ×10³μm2 单个胰岛面积输出显示在单个胰岛轮廓弹窗中，不显示在指标表格里 305077
			胰岛面积（全片）E mm²数据相加输出305077
			胰岛细胞核数量（单个）  F 个 单个胰岛内胰岛细胞核数量输出显示在单个胰岛轮廓弹窗中，不显示在指标表格里 305077、305078
			间质面积 G mm² 数据相加输出 305027
			导管面积 H mm² 数据相加输出 30506F
			血管面积 I mm² 数据相加输出 305003
			组织轮廓面积 J mm² 数据相加输出仅辅助指标10计算，数值不显示在页面指标表格里 305111
			胰岛细胞核数量（全片） K 个 数据相加输出 305077、305078
		 */
		//算法输出指标
		indicatorResultsMap.put("上皮细胞核数量", createIndicator(String.valueOf(A_305075_count), PIECE, "305075"));
		indicatorResultsMap.put("酶原颗粒面积", createIndicator(B_305076_organArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "305076"));
		indicatorResultsMap.put("胰岛数量", createIndicator(String.valueOf(C_305077_count), PIECE, "305077"));
		Annotation annotationBy = new Annotation();
		//D 胰岛面积（单个）  ×10³μm2 单个胰岛面积输出显示在单个胰岛轮廓弹窗中，不显示在指标表格里 305077
		annotationBy.setCountName(null);
		annotationBy.setAreaName("胰岛面积（单个）");
		annotationBy.setAreaUnit(SQ_UM_THOUSAND);
		commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "305077", annotationBy, 1);

		indicatorResultsMap.put("胰岛面积（全片）", createIndicator(E_305077_organArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "305077"));

		//F  胰岛细胞核数量（单个）   个 单个胰岛内胰岛细胞核数量输出显示在单个胰岛轮廓弹窗中，不显示在指标表格里 305077、305078
//		annotationBy.setCountName("胰岛细胞核数量（单个）");
//		commonJsonParser.putAnnotationDynamicData(jsonTask, "305077", "305078", annotationBy);

		indicatorResultsMap.put("间质面积", createIndicator(G_305027_organArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "305027"));
		indicatorResultsMap.put("导管面积", createIndicator(H_30506F_organArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "30506F"));
		indicatorResultsMap.put("血管面积", createIndicator(I_305003_organArea.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "305003"));
//		indicatorResultsMap.put("组织轮廓面积", createIndicator(J_organArea, SQ_MM, "301111"));
//		indicatorResultsMap.put("胰岛细胞核数量（全片）", createIndicator(String.valueOf(K_305078_count), PIECE, "305077,305078"));
		//        indicatorResultsMap.put("导管数量", createIndicator(String.valueOf(count2), PIECE, "30506F"));
		//indicatorResultsMap.put("导管细胞核数量（单个）", createDefaultIndicator("30506F,30507B"));
		//        indicatorResultsMap.put("血管内红细胞面积", createIndicator(annotationInner.getStructureAreaNum().setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "305003,305004"));
		//        indicatorResultsMap.put("血管外红细胞面积", createIndicator(annotationOuter.getStructureAreaNum().setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "305003,305004"));

		/**
		 * 
		 * 产品呈现指标指标代码（仅限本文档）单位（保留小数点后三位）English计算方式备注
			上皮细胞核密度 1  个/mm² Nucleus density of  epithelial cell 1=A/(J-E-G)
			酶原颗粒面积占比 2 % Zymogen granule area% 2=B/J
			胰岛面积占比3 % Pancreatic islet area% 3=E/J
			胰岛细胞核密度（单个）4  个/10³μm2 Nucleus density of pancreatic islet（per） 4=F/D以95%置信区间和均数±标准差呈现
			间质面积占比 5 % Mesenchyme area% 5=G/J
			导管面积占比 6 % Ducts area% 6=H/J
			血管面积占比 7 % Vessel area% 7=I/J
			腺泡面积占比 8 % Pancreatic acinus area% 8=（J-E-G）/J
			胰岛细胞核密度（全片）9  个/mm² Nucleus density of pancreatic islet（all） 9=K/E
			胰腺面积 10 mm² Pancreas area 10=J
		 */
		//产品定义指标
		BigDecimal J = new BigDecimal(singleSlide.getArea());
		//1 上皮细胞核密度 个/mm² 1=A/(J-E-G)
		if (J.subtract(E_305077_organArea).subtract(G_305027_organArea) != BigDecimal.ZERO) {
			BigDecimal result = bigDecimalDivideCheck(BigDecimal.valueOf(Long.valueOf(A_305075_count)), J.subtract(E_305077_organArea).subtract(G_305027_organArea));
			indicatorResultsMap.put("上皮细胞核密度", createNameIndicator("Nucleus density of  epithelial cell", result.toString(), SQ_MM_PIECE, "305075,305111,305077,305027"));
		}
		//2 酶原颗粒面积占比 % 2=B/J
		indicatorResultsMap.put("酶原颗粒面积占比", createNameIndicator("Zymogen granule area%", getProportion(B_305076_organArea, J).toString(), PERCENTAGE, "305076,305111"));
		// 3 胰岛面积占比 % 3=E/J
		indicatorResultsMap.put("胰岛面积占比", createNameIndicator("Pancreatic islet area%", getProportion(E_305077_organArea, J).toString(), PERCENTAGE, "305077,305111"));
		//4 胰岛细胞核密度（单个） 个/10³ μm2 4=F/D
		//indicatorResultsMap.put("胰岛细胞核密度（单个）", createNameIndicator("Nucleus density of pancreatic islet（per）", MathUtils.getConfidenceInterval(dataList), SQ_UM_PICE, "305077,305078"));
		//5 间质面积占比 % 5=G/J
		indicatorResultsMap.put("间质面积占比", createNameIndicator("Interstitial area%", getProportion(G_305027_organArea, J).toString(), PERCENTAGE, "305027,305111"));
		//6 导管面积占比 % 6=H/J
		indicatorResultsMap.put("导管面积占比", createNameIndicator("Vascular area%", getProportion(H_30506F_organArea, J).toString(), PERCENTAGE, "30506F,305111"));
		//7  血管面积占比  % Vessel area% 7=I/J
		indicatorResultsMap.put("血管面积占比", createNameIndicator("Vessel area%", getProportion(I_305003_organArea, J).toString(), PERCENTAGE, "305003,305111"));
		//8 腺泡面积占比 8 % Pancreatic acinus area% 8=（J-E-G）/J
		indicatorResultsMap.put("腺泡面积占比", createNameIndicator("Pancreatic acinus area%", getProportion(J.subtract(E_305077_organArea).subtract(G_305027_organArea), J).toString(), PERCENTAGE, "305077,305027,305111"));
		//9 胰岛细胞核密度（全片）9  个/mm² Nucleus density of pancreatic islet（all） 9=K/E
//		indicatorResultsMap.put("胰岛细胞核密度（全片）", createNameIndicator("Nucleus density of pancreatic islet（all）", bigDecimalDivideCheck(new BigDecimal(K_305078_count), E_305077_organArea).toString(), SQ_MM_PIECE, "305077,305078"));
		//10 胰腺面积 mm²  Pancreas area 10=J
		indicatorResultsMap.put("胰腺面积", createNameIndicator("Pancreas area", J_organArea, SQ_MM, "305111"));

		//        annotationList = getStructureContourList(jsonTask, "30506F");
		//        if (CollectionUtils.isNotEmpty(annotationList)) {
		//            dataList.clear();
		//            for (Annotation annotation : annotationList) {
		//                String contour = annotation.getContour();
		//                //K 导管细胞核数量（单个）个
		//                Annotation temp = getContourInsideOrOutside(jsonTask, contour, "30507B", true);
		//                if (annotation.getStructureAreaNum().compareTo(BigDecimal.ZERO) > 0 && temp.getCount() != 0) {
		//                    //7 导管细胞核密度（单个） 个/10³ μm2 7=K/I
		//                    dataList.add(BigDecimal.valueOf(temp.getCount()).divide(annotation.getStructureAreaNum().multiply(BigDecimal.valueOf(1000)), 3, RoundingMode.HALF_UP));
		//                }
		//            }
		//        }
		//indicatorResultsMap.put("胰腺面积", createNameIndicator("Pancreas area%", organAreaO.setScale(3, RoundingMode.DOWN).toString(), SQ_MM, "305111"));
		//indicatorResultsMap.put("导管细胞核密度（单个）", new IndicatorAddIn("Nucleus density of duct（per）", MathUtils.getConfidenceInterval(dataList), SQ_UM_PICE, CommonConstant.NUMBER_0, "30506F,30507B"));
		//8 血管内红细胞面积占比 % 8=M/O
		//        indicatorResultsMap.put("血管内红细胞面积占比", createNameIndicator("Intravascular erythrocyte area%", getProportion(annotationInner.getStructureAreaNum(), J).toString(), PERCENTAGE, "305003,305004,305111"));
		//9 血管外红细胞面积占比 % 9=N/O
		//        indicatorResultsMap.put("血管外红细胞面积占比", createNameIndicator("Extravascular erythrocyte area%", getProportion(annotationOuter.getStructureAreaNum(), J).toString(), PERCENTAGE, "305003,305004,305111"));

		//14 红细胞面积 mm² 14=Q
		//        indicatorResultsMap.put("红细胞面积", createNameIndicator("Erythrocyte area", organAreaQ, SQ_MM, "305004"));
		//        Annotation annotationBy = new Annotation();

		//        annotationBy.setCountName("导管细胞核数量（单个）");
		//        commonJsonParser.putAnnotationDynamicData(jsonTask, "30506F", "30507B", annotationBy);
		//I
		//        annotationBy.setAreaName("导管面积（单个）");
		//        annotationBy.setAreaUnit(SQ_UM_THOUSAND);
		//        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "30506F", annotationBy, 1);
		aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
	}

	@Override
	public String getAlgorithmCode() {
		return "Pancreas_3";
	}

}
