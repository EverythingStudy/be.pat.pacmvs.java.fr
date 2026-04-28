package cn.staitech.fr.service.strategy.json.impl.dog.urology;

import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.ProjectMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.MathUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * 
 * @ClassName: Kidney_3ParserStrategyImpl
 * @Description:犬-肾脏
 * @author wanglibei
 * @date 2026年2月11日
 * @version V1.0
 */
@Slf4j
@Service("Kidneys_3")
public class Kidney_3ParserStrategyImpl extends AbstractCustomParserStrategy {
	@Resource
	private SingleSlideMapper singleSlideMapper;
	@Resource
	private AiForecastService aiForecastService;
	@Resource
	private CommonJsonParser commonJsonParser;
	@Resource
	private CommonJsonCheck commonJsonCheck;
	@Resource
	private ProjectMapper projectMapper;
	@Resource(name = "dynamicDataThreadPool")
	private ExecutorService dynamicDataThreadPool;

	@PostConstruct
	public void init() {
		setCommonJsonParser(commonJsonParser);
		setCommonJsonCheck(commonJsonCheck);
		log.debug("Kidney_3ParserStrategyImpl init");
	}
	/**
	 * 

    结构编码
    髓质31B03E
    皮质31B03D
    肾小球31B02D
    血管球31B026
    肾小管31B031
    球内基质31B030
    球内细胞核31B02E
    球内红细胞31B02F
    组织轮廓31B111

    算法输出指标指标代码（仅限本文档）单位（保留小数点后三位）备注
    皮质面积 A 平方毫米
    肾小球面积（单个） B 10³平方微米
    肾小球面积（全片）C 平方毫米 数据相加输出
    球内细胞核数量（单个）D 个 单个肾小球内数据相加输出
    球内细胞核数量（全片）E 个 数据相加输出
    球内红细胞面积（单个）F 10³ 平方微米 单个肾小球内数据相加输出
    球内红细胞面积（全片）G 平方毫米 数据相加输出
    肾小管面积(单个)H 10³ 平方微米
    肾小管面积（全片）I 平方毫米 数据相加输出
    血管球面积（单个）J 10³平方微米
    血管球面积（全片）K 平方毫米数 据相加输出
    组织轮廓面积 L 平方毫米
	 */

	@Override
	public void alculationIndicators(JsonTask jsonTask) {
		//A 皮质面积
		Annotation a_31B03D_anno = getOrganArea(jsonTask, "31B03D");
		BigDecimal A_31B03D = a_31B03D_anno.getStructureAreaNum();
		
		//M 髓质面积		M		平方毫米
		BigDecimal M_31B03E_area = commonJsonParser.getOrganArea(jsonTask, "31B03E").getStructureAreaNum();

		//B 肾小球面积（单个）10³平方微米
		Annotation annotationB = new Annotation();
		annotationB.setAreaName("肾小球面积（单个）");
		annotationB.setAreaUnit(SQ_UM_THOUSAND);
		commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "31B02D", annotationB, 1);

		//C 肾小球面积（全片） 平方毫米		数据相加输出
		BigDecimal C_31B02D_area = commonJsonParser.getOrganArea(jsonTask, "31B02D").getStructureAreaNum();

		//D 球内细胞核数量（单个）(31B02E)                    单个肾小球(31B02D) 内数据相加输出
//		Annotation annotationD = new Annotation();
//		annotationD.setCountName("球内细胞核数量（单个）");
//		annotationD.setAreaUnit(PIECE);
//		commonJsonParser.putAnnotationDynamicData(jsonTask, "31B02D", "31B02E", annotationD);

		//E 球内细胞核数量（全片）个		数据相加输出
//		Integer E_31B02E_count = commonJsonParser.getOrganAreaCount(jsonTask, "31B02E");
//		BigDecimal E_31B02E_area = commonJsonParser.getOrganArea(jsonTask, "31B02E").getStructureAreaNum();    

		//F 球内红细胞面积（单个） 10³平方微米		 单个肾小球内数据相加输出
//		Annotation annotationF = new Annotation();
//		annotationF.setAreaName("球内红细胞面积（单个）");
//		annotationF.setAreaUnit(CommonConstant.SQUARE_MICROMETER);
//		commonJsonParser.putAnnotationDynamicData(jsonTask, "31B02D", "31B02F", annotationF, 1, true);

		//G 球内红细胞面积（全片）平方毫米 数据相加输出
//		BigDecimal G_31B02F_area = commonJsonParser.getOrganArea(jsonTask, "31B02F").getStructureAreaNum();

		
		//H 肾小管面积(单个)  10³平方微米
		Annotation annotationByH = new Annotation();
		annotationByH.setAreaName("肾小管面积(单个)");
		annotationByH.setAreaUnit(MULTIPLIED_SQ_UM_THOUSAND);
		//		commonJsonParser.putAnnotationDynamicData(jsonTask, "107088", "31B031", annotationByH, 1, true);
		commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "31B031", annotationByH, 1);

		//I 肾小管面积（全片）      平方毫米        数据相加输出
		BigDecimal I_31B031_area = commonJsonParser.getOrganArea(jsonTask, "31B031").getStructureAreaNum();    

		//J  血管球面积（单个）10³平方微米
//		Annotation annotationByJ = new Annotation();
//		annotationByJ.setAreaName("血管球面积（单个）");
//		annotationByJ.setAreaUnit(SQ_UM_THOUSAND);
//		commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "31B026", annotationByJ, 1);

		//K 血管球面积（全片） 平方毫米  数据相加输出
		//BigDecimal K_31B026_area = commonJsonParser.getOrganArea(jsonTask, "31B026").getStructureAreaNum();

		//L 组织轮廓-肾脏面积 平方毫米
		SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
		BigDecimal L_31B111 = new BigDecimal(0);
		if (ObjectUtil.isNotEmpty(singleSlide) && StringUtils.isNotEmpty(singleSlide.getArea())) {
			L_31B111 = L_31B111.add(new BigDecimal(singleSlide.getArea()));
		}

		
		
		Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
		/**
		 * 
		算法输出指标指标代码（仅限本文档）单位（保留小数点后三位）备注相关结构
		皮质面积A平方毫米31B03D
		髓质面积M平方毫米31B03E
		肾小球面积（单个）B10³平方微米31B02D
		肾小球面积（全片）C平方毫米数据相加输出31B02D
		球内细胞核数量（单个）D个单个肾小球内数据相加输出31B02D、31B02E
		球内细胞核数量（全片）E个数据相加输出31B02D、31B02E
		球内红细胞面积（单个）F10³平方微米单个肾小球内数据相加输出31B02D、31B02F
		球内红细胞面积（全片）G平方毫米数据相加输出31B02D、31B02F
		肾小管面积(单个)H10³平方微米31B031
		肾小管面积（全片）I平方毫米数据相加输出31B031
		血管球面积（单个）J10³平方微米31B026
		血管球面积（全片）K平方毫米数据相加输出31B026
		组织轮廓面积L平方毫米仅辅助指标1计算，数值不显示在页面指标表格里31B111
		 */
		//		A_31B03D C_31B02D_area E_31B02E_count G_31B02F_area I_31B031_area K_31B026_area L_31B111 M_31B03E_area
		//一级指标（算法输出指标）
		
		//A 皮质面积 平方毫米 31B03D
		indicatorResultsMap.put("皮质面积", createIndicator(A_31B03D.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "31B03D"));
	    //M 髓质面积 平方毫米 31B03E
		indicatorResultsMap.put("髓质面积", createIndicator(M_31B03E_area.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "31B03E"));
		//C 肾小球面积（全片）平方毫米 数据相加输出 31B02D
		indicatorResultsMap.put("肾小球面积（全片）", createIndicator(C_31B02D_area.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "31B02D"));
		//E 球内细胞核数量（全片）个 数据相加输出 31B02D、31B02E
//		indicatorResultsMap.put("球内细胞核数量（全片）", createIndicator(C_31B02D_area.setScale(3, RoundingMode.HALF_UP).toString(), PIECE, "31B02D,31B02E"));
		//G 球内红细胞面积（全片）平方毫米 数据相加输出31B02D、31B02F
//		indicatorResultsMap.put("球内红细胞面积（全片）", createIndicator(G_31B02F_area.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "31B02D,31B02F"));
		//I 肾小管面积（全片）平方毫米 数据相加输出 31B031
		indicatorResultsMap.put("肾小管面积（全片）", createIndicator(I_31B031_area.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "31B031"));
		//K 血管球面积（全片）平方毫米 数据相加输出 31B026
//		indicatorResultsMap.put("血管球面积（全片）", createIndicator(K_31B026_area.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "31B026"));
		//L 组织轮廓面积 平方毫米 仅辅助指标1计算，数值不显示在页面指标表格里 31B111
//		indicatorResultsMap.put("组织轮廓面积", createIndicator(L_31B111.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "31B111"));

		//二级指标（产品呈现指标）
		/**
		 *
		产品呈现指标指标代码（仅限本文档）单位（保留小数点后三位）English计算方式备注
		肾脏面积1平方毫米 Renal area1=L
		皮质面积占比2%Cortical area% 2=A/L
		髓质面积占比3%Medulla area%3=M/L
		球内细胞核密度（单个）4个/10³平方微米Nucleus density of glomerulus (per)4=D/J单个为单个肾小球以95%置信区间和均数±标准差呈现
		球内细胞核密度（全片）5个/平方毫米Nucleus density of glomerulus (all)5=E/K
		球内红细胞面积占比（单个）6%Erythrocyte of glomerulus area% (per)6=F/J单个为单个肾小球以95%置信区间和均数±标准差呈现
		球内红细胞面积占比（全片）7%Erythrocyte of glomerulus area% (all)7=G/K
		皮质肾小球面积占比（全片）8%Glomerulus of renal cortical area%（all）8=C/A
		皮质肾小管面积占比9% Tubules of renal cortical  area%9=I/A
		肾小管面积(单个)10³平方微米Renal tubule area (per)10=H以95%置信区间和均数±标准差呈现
		血管球面积占比（单个）11%Glomerulus area%（per）11=J/B配对肾小球和血管球以95%置信区间和均数±标准差呈现
		血管球面积占比(全片)12%Glomerulus area%（all）12=K/C
		肾小囊面积占比（单个）13%Renal capsule area%（per）13=(B-J)/B配对肾小球和血管球以95%置信区间和均数±标准差呈现
		肾小囊面积占比（全片）14%Renal capsule area%（all）14=(C-K)/C
		球内基质面积占比（单个）15%Mesangial matrix area%（per）15=(J-D-F)/J以95%置信区间和均数±标准差呈现
		球内基质面积占比（全片）16%Mesangial matrix area%（all）16=(K-E-G)/K
		 */
		//A_31B03D C_31B02D_area E_31B02E_count G_31B02F_area I_31B031_area K_31B026_area L_31B111 M_31B03E_area
		//肾脏面积 1 平方毫米 Renal area1=L
		indicatorResultsMap.put("肾脏面积", createNameIndicator("Renal area", L_31B111, SQ_MM, "31B111"));
		//皮质面积占比 2 % Cortical area% 2=A/L
		indicatorResultsMap.put("皮质面积占比", createNameIndicator("Cortical area%", String.valueOf(getProportion(A_31B03D, L_31B111)), PERCENTAGE, "31B03D,31B111"));
		//髓质面积占比 3 % Medulla area% 3=M/L
		indicatorResultsMap.put("髓质面积占比", createNameIndicator("Medulla area%", String.valueOf(getProportion(M_31B03E_area, L_31B111)), PERCENTAGE, "31B03E,31B111"));
		
		//血管球
//		List<Annotation> annotationList = commonJsonParser.getStructureContourList(jsonTask, "31B026");

		//球内细胞核密度（单个）4 个/10³平方微米 Nucleus density of glomerulus (per) 4=D/J 单个为单个肾小球以95%置信区间和均数±标准差呈现
//		String mucosalCellDensity = getResult1(jsonTask, "31B026", "31B02E",annotationList);
//		indicatorResultsMap.put("球内细胞核密度（单个）", createNameIndicator("Nucleus density of glomerulus (per)", mucosalCellDensity, SQ_UM_PICE, "31B02D,31B02E,31B026"));

		//球内细胞核密度（全片）5 个/平方毫米 Nucleus density of glomerulus (all) 5=E/K
//		indicatorResultsMap.put("球内细胞核密度（全片）", createNameIndicator("Nucleus density of glomerulus (all)", String.valueOf(getProportion(new BigDecimal(E_31B02E_count), K_31B026_area)), SQ_MM_PIECE, "31B03D,31B111"));

		//球内红细胞面积占比（单个）6 % Erythrocyte of glomerulus area% (per) 6=F/J 单个为单个肾小球以95%置信区间和均数±标准差呈现
//		String ieaf = getResult2(jsonTask, "31B02F", "31B026",annotationList);
//		indicatorResultsMap.put("球内红细胞面积占比（单个）", createNameIndicator("Erythrocyte of glomerulus area% (per)", ieaf, PERCENTAGE, "31B02F,31B026"));

		//球内红细胞面积占比（全片）7 % Erythrocyte of glomerulus area% (all) 7=G/K
//		indicatorResultsMap.put("球内红细胞面积占比（全片）", createNameIndicator("Erythrocyte of glomerulus area%", String.valueOf(getProportion(G_31B02F_area, K_31B026_area)), PERCENTAGE, "31B02F,31B026"));

		//皮质肾小球面积占比（全片）8 % Glomerulus of renal cortical area%（all） 8=C/A
		indicatorResultsMap.put("皮质肾小球面积占比（全片）", createNameIndicator("Glomerulus of renal cortical area%（all）", String.valueOf(getProportion(C_31B02D_area, A_31B03D)), PERCENTAGE, "31B02D,31B03D"));

		//皮质肾小管面积占比9 % Tubules of renal cortical  area% 9=I/A
		indicatorResultsMap.put("皮质肾小管面积占比", createNameIndicator("Tubules of renal cortical  area%", String.valueOf(getProportion(I_31B031_area, A_31B03D)), PERCENTAGE, "31B031,31B03D"));
		
		//肾小管面积(单个)10 10³平方微米 Renal tubule area (per)10=H 以95%置信区间和均数±标准差呈现
		String itaS = getSingleResult(jsonTask);
		indicatorResultsMap.put("肾小管面积(单个)", createNameIndicator("Renal tubule area (per)", itaS, SQ_UM_THOUSAND, "31B031"));
		
		
		//血管球面积占比（单个）11 % Glomerulus area%（per）11=J/B 配对肾小球和血管球以95%置信区间和均数±标准差呈现
//		String gcafs = getResult2(jsonTask, "31B026", "31B02D",annotationList);
//		indicatorResultsMap.put("血管球面积占比（单个）", createNameIndicator("Glomerulus area%（per）", gcafs, PERCENTAGE, "31B026,31B02D"));

		//血管球面积占比(全片)12 % Glomerulus area%（all）12=K/C
//		indicatorResultsMap.put("血管球面积占比(全片)", createNameIndicator("Glomerulus area%", String.valueOf(getProportion(K_31B026_area, C_31B02D_area)), PERCENTAGE, "31B026,31B02D"));
		
		//肾小囊面积占比（单个）13 % Renal capsule area%（per）13=(B-J)/B 配对肾小球和血管球以95%置信区间和均数±标准差呈现
//		String bcafs = getResult3(jsonTask, "31B026", "31B02D",annotationList);
//		indicatorResultsMap.put("肾小囊面积占比（单个）", createNameIndicator("Renal capsule area%（per）", gcafs, PERCENTAGE, "31B026,31B02D"));

		//肾小囊面积占比（全片）14 % Renal capsule area%（all）14=(C-K)/C
//		indicatorResultsMap.put("肾小囊面积占比（全片）", createNameIndicator("Renal capsule area%（all）", String.valueOf(getProportion(C_31B02D_area.subtract(K_31B026_area), A_31B03D)), PERCENTAGE, "31B02D,31B026,31B03D"));
		
		//球内基质面积占比（单个）15 % Mesangial matrix area%（per）15=(J-D-F)/J 以95%置信区间和均数±标准差呈现
//		String sbcaf = getResult4(jsonTask, "31B026", "31B02F", "31B02E",annotationList);
//		indicatorResultsMap.put("球内基质面积占比（单个）", createNameIndicator("Glomerulus area%（per）", sbcaf, PERCENTAGE, "31B026,31B02F,31B02E"));
		
		//球内基质面积占比（全片）16 % Mesangial matrix area%（all）16=(K-E-G)/K
//		indicatorResultsMap.put("球内基质面积占比（全片）", createNameIndicator("Mesangial matrix area%（all）", String.valueOf(getProportion(K_31B026_area.subtract(E_31B02E_area).subtract(G_31B02F_area), K_31B026_area)), PERCENTAGE, "31B026,31B02E,31B02F"));
		
		//A_31B03D C_31B02D_area E_31B02E_count G_31B02F_area I_31B031_area K_31B026_area L_31B111 M_31B03E_area
		aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
	}
	
	//球内细胞核密度（单个）
	private String getResult1(JsonTask jsonTask, String entireStructureId, String internalStructureId,List<Annotation> annotationList) {
//		List<Annotation> annotationList = commonJsonParser.getStructureContourList(jsonTask, entireStructureId);

		if (CollectionUtils.isEmpty(annotationList)) {
			return MathUtils.getConfidenceInterval(Collections.emptyList());
		}

		List<CompletableFuture<BigDecimal>> futures = new ArrayList<>(annotationList.size());

		for (Annotation annotation : annotationList) {
			CompletableFuture<BigDecimal> future = CompletableFuture.supplyAsync(() -> {
				try {
					// 获取内部结构（如细胞核）的统计信息
					Annotation insideResult = commonJsonParser.getContourInsideOrOutside(jsonTask,
							annotation.getContour(), internalStructureId, true);

					Integer count = (insideResult != null) ? insideResult.getCount() : 0;
					BigDecimal area = annotation.getStructureAreaNum();

					// 防止除零或 null
					if (area == null || area.compareTo(BigDecimal.ZERO) <= 0) {
						return BigDecimal.ZERO;
					}

					return commonJsonParser.bigDecimalDivideCheck(BigDecimal.valueOf(count), area);
				} catch (Exception e) {
					log.warn("计算单个注解密度失败，annotationId: {}", annotation.getAnnotationId(), e);
					return null; // 后续会被 filter 掉
				}
			}, dynamicDataThreadPool);

			futures.add(future);
		}

		// 等待所有任务完成（即使部分失败）
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

		// 收集非 null 结果
		List<BigDecimal> results = futures.stream().map(CompletableFuture::join).filter(Objects::nonNull).collect(Collectors.toList());

		return MathUtils.getConfidenceInterval(results);
	}
	
	//球内红细胞面积占比（单个） 6=F/J
	private String getResult2(JsonTask jsonTask, String entireStructureId, String internalStructureId,List<Annotation> annotationList ) {
//		List<Annotation> annotationList1 = commonJsonParser.getStructureContourList(jsonTask, entireStructureId);

		if (CollectionUtils.isEmpty(annotationList)) {
			return MathUtils.getConfidenceInterval(Collections.emptyList());
		}

		List<CompletableFuture<BigDecimal>> futures = new ArrayList<>();

		for (Annotation annotation : annotationList) {
			CompletableFuture<BigDecimal> future = CompletableFuture.supplyAsync(() -> {
				// 球内红细胞面积（单位：平方微米）
				BigDecimal structureAreaF = annotation.getStructureAreaNum().multiply(BigDecimal.valueOf(1000));

				// 血管球内目标结构（如红细胞）的面积
				Annotation insideAnnotation = commonJsonParser.getContourInsideOrOutside(jsonTask,
						annotation.getContour(), internalStructureId, true);
				BigDecimal structureAreaNumJ = insideAnnotation.getStructureAreaNum()
						.multiply(BigDecimal.valueOf(1000));

				// 计算占比：红细胞面积 / 血管球面积
				return getProportion(structureAreaF, structureAreaNumJ);
			}, dynamicDataThreadPool);

			futures.add(future);
		}

		// 等待所有异步任务完成
		CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
		try {
			allDone.join();
		} catch (Exception e) {
			log.error("并行处理 getResult2 数据失败", e);
		}

		// 收集结果（过滤 null）
		List<BigDecimal> results = futures.stream().map(CompletableFuture::join).filter(Objects::nonNull).collect(Collectors.toList());

		return MathUtils.getConfidenceInterval(results);
	}
	
	//肾小囊面积占比（单个）13=(B-J)/B
	private String getResult3(JsonTask jsonTask, String entireStructureId, String internalStructureId,List<Annotation> annotationListO ) {
		List<Annotation> annotationList = commonJsonParser.getStructureContourList(jsonTask, internalStructureId);

		if (CollectionUtils.isEmpty(annotationList)) {
			return MathUtils.getConfidenceInterval(Collections.emptyList());
		}

		List<CompletableFuture<BigDecimal>> futures = new ArrayList<>();

		for (Annotation annotation : annotationList) {
			CompletableFuture<BigDecimal> future = CompletableFuture.supplyAsync(() -> {
				// 球内红细胞面积（单位：平方微米）
				BigDecimal structureAreaB = annotation.getStructureAreaNum().multiply(BigDecimal.valueOf(1000));

				// 血管球内目标结构（如红细胞）的面积
				Annotation insideAnnotation = commonJsonParser.getContourInsideOrOutside(jsonTask,annotation.getContour(), entireStructureId, true);
				BigDecimal structureAreaNumJ = insideAnnotation.getStructureAreaNum().multiply(BigDecimal.valueOf(1000));

				// 计算占比：红细胞面积 / 血管球面积
				return getProportion(structureAreaB.subtract(structureAreaNumJ), structureAreaB);
			}, dynamicDataThreadPool);

			futures.add(future);
		}

		// 等待所有异步任务完成
		CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
		try {
			allDone.join();
		} catch (Exception e) {
			log.error("并行处理 getResult2 数据失败", e);
		}

		// 收集结果（过滤 null）
		List<BigDecimal> results = futures.stream().map(CompletableFuture::join).filter(Objects::nonNull).collect(Collectors.toList());

		return MathUtils.getConfidenceInterval(results);
	}
	
	//球内基质面积占比（单个）15=(J-D-F)/J
	private String getResult4(JsonTask jsonTask, String entireStructureId, String internalStructureId1, String internalStructureId2,List<Annotation> annotationList ) {
//		List<Annotation> annotationList = commonJsonParser.getStructureContourList(jsonTask, internalStructureId);

		if (CollectionUtils.isEmpty(annotationList)) {
			return MathUtils.getConfidenceInterval(Collections.emptyList());
		}

		List<CompletableFuture<BigDecimal>> futures = new ArrayList<>();

		for (Annotation annotation : annotationList) {
			CompletableFuture<BigDecimal> future = CompletableFuture.supplyAsync(() -> {
				// 球内红细胞面积（单位：平方微米）
				BigDecimal structureAreaJ = annotation.getStructureAreaNum().multiply(BigDecimal.valueOf(1000));

				// 血管球内目标结构（如红细胞）的面积
				Annotation insideAnnotation1 = commonJsonParser.getContourInsideOrOutside(jsonTask,annotation.getContour(), internalStructureId1, true);
				BigDecimal structureAreaNumD1 = insideAnnotation1.getStructureAreaNum().multiply(BigDecimal.valueOf(1000));
				
				// 血管球内目标结构（如红细胞）的面积2
				Annotation insideAnnotation2 = commonJsonParser.getContourInsideOrOutside(jsonTask,annotation.getContour(), internalStructureId1, true);
				BigDecimal structureAreaNumD2 = insideAnnotation2.getStructureAreaNum().multiply(BigDecimal.valueOf(1000));

				// 计算占比：红细胞面积 / 血管球面积
				return getProportion(structureAreaJ.subtract(structureAreaNumD1).subtract(structureAreaNumD2), structureAreaJ);
			}, dynamicDataThreadPool);

			futures.add(future);
		}

		// 等待所有异步任务完成
		CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
		try {
			allDone.join();
		} catch (Exception e) {
			log.error("并行处理 getResult2 数据失败", e);
		}

		// 收集结果（过滤 null）
		List<BigDecimal> results = futures.stream().map(CompletableFuture::join).filter(Objects::nonNull).collect(Collectors.toList());

		return MathUtils.getConfidenceInterval(results);
	}
	
	
	private String getSingleResult(JsonTask jsonTask) {
		List<BigDecimal> list = new ArrayList<>();
		List<Annotation> structureContourList = getStructureContourList(jsonTask, "31B031");
		if (CollectionUtils.isNotEmpty(structureContourList)) {
			for (Annotation annotation : structureContourList) {
				// A 甲状腺滤泡面积（单个）	A	103平方微米	单个甲状腺滤泡（107088）面积
				BigDecimal structureAreaNumA = annotation.getStructureAreaNum().multiply(new BigDecimal(1000));
				list.add(structureAreaNumA);
			}
		}

        return MathUtils.getConfidenceInterval(list);
	}

	@Override
	public String getAlgorithmCode() {
		return "Kidneys_3";
	}
}
