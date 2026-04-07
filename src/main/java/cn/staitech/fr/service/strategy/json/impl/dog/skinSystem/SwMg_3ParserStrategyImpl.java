package cn.staitech.fr.service.strategy.json.impl.dog.skinSystem;

import cn.hutool.core.collection.CollectionUtil;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.SingleSlideService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import cn.staitech.fr.utils.DecimalUtils;
import cn.staitech.fr.utils.MathUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wanglibei
 * @version V1.0
 * @ClassName: MammaryGlandParserStrategyImpl
 * @Description-d:犬-皮肤（乳腺附近）及乳腺
 * @date 2025年7月22日
 */
@Slf4j
@Component("Skin_with_mammary_gland_3")
public class SwMg_3ParserStrategyImpl extends AbstractCustomParserStrategy {

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
	private SingleSlideService singleSlideService;

	@PostConstruct
	public void init() {
		setCommonJsonParser(commonJsonParser);
		setCommonJsonCheck(commonJsonCheck);
		log.info("SwMg_3ParserStrategyImpl init");
	}

	/**
	 * 指标计算
	 *
	 * @param jsonTask
	 */
	@Override
	public void alculationIndicators(JsonTask jsonTask) {
		log.info("指标计算开始-皮肤（乳腺附近）及乳腺");

		//-------------------乳腺--------------------------------------
		// A 皮肤与乳腺总面积  mm² 仅辅助指标6、7、9、10、11、12、13计算，数值不显示在页面指标表格里 37A111
//		SingleSlide singleSlidea = singleSlideMapper.selectById(jsonTask.getSingleId());
//		BigDecimal A_37A111_area = new BigDecimal(singleSlidea.getArea());

		//B 乳腺腺泡/导管数量  个 无 37A06C
		Integer B_37A06C_count = commonJsonParser.getOrganAreaCount(jsonTask, "37A06C");

		//C 乳腺腺泡/导管面积（全片） mm² 数据相加输出 37A06C
		BigDecimal C_37A06C_area = commonJsonParser.getOrganArea(jsonTask, "37A06C").getStructureAreaNum();

		//D 乳腺细胞核数量（单个）个 单个腺泡/导管内细胞核数量输出显示在单个腺泡/导管轮廓弹窗中，不显示在指标表格里 37A06C、37A061
//		Annotation annotationD = new Annotation();
//		annotationD.setCountName("乳腺细胞核数量（单个）");
//		commonJsonParser.putAnnotationDynamicData(jsonTask, "37A06C", "37A061", annotationD);

		//E 乳腺腺泡/导管面积（单个）×10³ μm2 单个腺泡/导管面积输出显示在单个腺泡/导管轮廓弹窗中，不显示在指标表格里 37A06C
		Annotation annotationE = new Annotation();
		annotationE.setAreaName("乳腺腺泡和导管面积（单个）");
		annotationE.setAreaUnit(MULTIPLIED_SQ_UM_THOUSAND);
		commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "37A06C", annotationE, 1);

		//F 乳腺面积 mm² 仅辅助指标5计算，数值不显示在页面指标表格里 37A07A
		SingleSlide singleSlideF = singleSlideService.getSingleSlide(jsonTask.getSingleId(), jsonTask.getImageId(), "37A07A");
		BigDecimal F_37A07A_area = BigDecimal.ZERO;
		if(null != singleSlideF) {
			F_37A07A_area = new BigDecimal(singleSlideF.getArea());
		}

		//G 乳腺细胞核数量（全片）个 37A061
//		Integer G_37A061_count = commonJsonParser.getOrganAreaCount(jsonTask, "37A061");

		//-------------------皮肤--------------------------------------
		/**
		 * 算法输出指标 指标代码（仅限本文档） 单位（保留小数点后三位） 备注 相关指标
		 * 皮肤
		    H 表皮角质层面积 mm² 若多个数据则相加输出 37A096
			I 表皮颗粒层+棘层+基底细胞层面积 mm² 若多个数据则相加输出 37A097
			J 毛囊面积（单个）×10³ μm2 单个毛囊面积输出显示在单个毛囊轮廓弹窗中，不显示在指标表格里 37A098
			K 毛囊数量  个 无 37A098
			L 皮脂腺面积 ×10³ μm2 数据相加输出 37A099
			M 皮脂腺数量 个 无 37A099
			N 皮肤面积 mm² 仅辅助指标13计算，数值不显示在页面指标表格里 37A0C3
			O 毛囊面积（全片）mm² 无 37A098
		 */
		//H 表皮角质层面积 mm² 若多个数据则相加输出 37A096
		BigDecimal H_37A096_area = commonJsonParser.getOrganArea(jsonTask, "37A096").getStructureAreaNum();

		// I 表皮颗粒层+棘层+基底细胞层面积 mm² 若多个数据则相加输出 37A097
		BigDecimal I_37A097_area = commonJsonParser.getOrganArea(jsonTask, "37A097").getStructureAreaNum();

		//J 毛囊面积（单个）×10³ μm2 单个毛囊面积输出显示在单个毛囊轮廓弹窗中，不显示在指标表格里 37A098
		Annotation annotationJ = new Annotation();
		annotationJ.setAreaName("毛囊面积（单个）");
		annotationJ.setAreaUnit(MULTIPLIED_SQ_UM_THOUSAND);
		commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "37A098", annotationJ, 1);

		// K 毛囊数量  个 无 37A098 
		Integer K_37A098_count = commonJsonParser.getOrganAreaCount(jsonTask, "37A098");

		// L 皮脂腺面积 ×10³ μm2 数据相加输出 37A099 
		BigDecimal L_37A099_area = commonJsonParser.getOrganAreaMicron(jsonTask, "37A099");

		// M 皮脂腺数量 个 无 37A099
		Integer M_37A099_count = commonJsonParser.getOrganAreaCount(jsonTask, "37A099");

		//N 皮肤面积 mm² 仅辅助指标13计算，数值不显示在页面指标表格里 37A0C3
		SingleSlide singleSlideN_ = singleSlideService.getSingleSlide(jsonTask.getSingleId(), jsonTask.getImageId(), "37A0C3");
		BigDecimal N_37A0C3_area = BigDecimal.ZERO;
		if(null != singleSlideN_) {
			N_37A0C3_area = new BigDecimal(singleSlideN_.getArea());
		}

		//O 毛囊面积（全片）mm² 无 37A098
		BigDecimal O_37A098_area = commonJsonParser.getOrganArea(jsonTask, "37A098").getStructureAreaNum();

		Map<String, IndicatorAddIn> map = new HashMap<>();
		//---------------算法输出指标-----------
		/**
		 * 
		 * 算法输出指标 指标代码（仅限本文档） 单位（保留小数点后三位） 备注 相关指标
		 */
		//A 皮肤与乳腺总面积  mm² 仅辅助指标6、7、9、10、11、12、13计算，数值不显示在页面指标表格里 37A111
		//map.put("皮肤与乳腺总面积", createIndicator(DecimalUtils.setScale3(A_37A111_area).toString(), SQ_MM, "37A111"));
		//B 乳腺腺泡/导管数量  个 无 37A06C
		map.put("乳腺腺泡/导管数量", createIndicator(B_37A06C_count.toString(), PIECE, "37A06C"));
		//C 乳腺腺泡/导管面积（全片） mm² 数据相加输出 37A06C
		map.put("乳腺腺泡/导管面积（全片）", createIndicator(C_37A06C_area.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "37A06C"));
		//F 乳腺面积 mm² 仅辅助指标5计算，数值不显示在页面指标表格里 37A07A
		//map.put("乳腺面积", createIndicator(F_37A07A_area, SQ_MM, "37A07A"));
		//G 乳腺细胞核数量（全片）个 37A061
		//map.put("乳腺细胞核数量（全片）", createNameIndicator(G_37A061_count.toString(), PIECE, "37A061"));

		/**
		 * 算法输出指标 指标代码（仅限本文档） 单位（保留小数点后三位） 备注 相关指标
		 */
		//H 表皮角质层面积 mm² 若多个数据则相加输出 37A096
		map.put("表皮角质层面积", createIndicator(H_37A096_area, SQ_MM, "37A096"));
		//I 表皮颗粒层+棘层+基底细胞层面积 mm² 若多个数据则相加输出 37A097
		map.put("表皮颗粒层+棘层+基底细胞层面积", createIndicator(I_37A097_area, SQ_MM, "37A097"));
		//J 毛囊面积（单个）×10³ μm2 单个毛囊面积输出显示在单个毛囊轮廓弹窗中，不显示在指标表格里 37A098
		//K 毛囊数量  个 无 37A098
		map.put("毛囊数量", createIndicator(K_37A098_count.toString(), PIECE, "37A098"));
		//L 皮脂腺面积 ×10³ μm2 数据相加输出 37A099
		map.put("皮脂腺面积", createIndicator(L_37A099_area, MULTIPLIED_SQ_UM_THOUSAND, "37A099"));
		//M 皮脂腺数量 个 无 37A099
		map.put("皮脂腺数量", createIndicator(M_37A099_count.toString(), PIECE, "37A099"));
		//N 皮肤面积 mm² 仅辅助指标13计算，数值不显示在页面指标表格里 37A0C3
		//map.put("皮肤面积", createIndicator(N_37A0C3_area, SQ_MM, "37A0C3"));
		//O 毛囊面积（全片）mm² 无 37A098
		map.put("毛囊面积（全片）", createIndicator(O_37A098_area, SQ_MM, "37A098"));



		//		if (organAreaJ.compareTo(new BigDecimal(0)) != 0) {
		//		}
		//----------------产品呈现指标----------------------------
		/**
		 * 产品呈现指标 指标代码（仅限本文档） 单位（保留小数点后三位） English 计算方式 备注
		 */

		//1 乳腺腺泡和导管数量 个 Number of acinus and ducts 1=B
		map.put("乳腺腺泡和导管数量", createNameIndicator("Number of acinus and ducts", B_37A06C_count.toString(), PIECE, "37A06C"));
		//2 乳腺腺泡和导管面积占比 % Acinus and ducts area% 2=C/F
		map.put("乳腺腺泡和导管面积占比", createNameIndicator("Acinus and ducts area%", getProportion(C_37A06C_area, F_37A07A_area), PERCENTAGE, areaUtils.getStructureIds("37A06C", "37A07A")));
		//3 腺泡或导管细胞核密度（单个）个/10³ μm2 Nucleus density of acinus or ducts （per）3=D/E以95%置信区间和均数±标准差呈现  (下面的是用于计算的)
			//D 乳腺细胞核数量（单个）个 单个腺泡/导管内细胞核数量输出显示在单个腺泡/导管轮廓弹窗中，不显示在指标表格里 37A06C、37A061
			//E 乳腺腺泡/导管面积（单个）×10³ μm2 单个腺泡/导管面积输出显示在单个腺泡/导管轮廓弹窗中，不显示在指标表格里 37A06C
		/**
		 * 
		 
		List<Annotation> structureContourList = commonJsonParser.getStructureContourList(jsonTask, "37A06C");
		List<BigDecimal> lists = new ArrayList<>();
		if (CollectionUtil.isNotEmpty(structureContourList)) {
			for (Annotation annotation : structureContourList) {
				//E 乳腺腺泡/导管面积（单个）
				BigDecimal structureAreaNum = annotation.getStructureAreaNum();
				//D 乳腺细胞核数量（单个）个
				Annotation contourInsideOrOutside = commonJsonParser.getContourInsideOrOutside(jsonTask, annotation.getContour(), "37A061", true);
				Integer count = contourInsideOrOutside.getCount();
				if (structureAreaNum.signum() != 0) {
					BigDecimal multiply = structureAreaNum.multiply(new BigDecimal(1000));
					lists.add(new BigDecimal(count).divide(multiply, 10, RoundingMode.HALF_UP));
				}
			}
		}
      	map.put("腺泡或导管细胞核密度（单个）", createNameIndicator(MathUtils.getConfidenceInterval(lists), SQ_UM_PICE, "37A06C,37A061"));
		 */
		//4 乳腺细胞核密度（全片）个/mm² Nucleus density of mammary gland（all）4=G/C
		//map.put("乳腺细胞核密度（全片）", createNameIndicator("Nucleus density of mammary gland（all）", getProportion(new BigDecimal(G_37A061_count), C_37A06C_area), SQ_MM_PIECE, "37A061,37A06C"));
		//5 乳腺面积 mm² Mammary gland area 5=F
		map.put("乳腺面积", createNameIndicator("Mammary gland area", F_37A07A_area, SQ_MM, "37A07A"));


		/**
		 *
		 *皮肤
		    6 表皮角质层面积占比 % Stratum corneum area% 6=H/(A-F)
			7 表皮颗粒层+棘层+基底细胞层面积占比 % Nucleated cell layer area% 7=I/(A-F)
			8 毛囊面积（单个）×10³ μm2 Hair follicle area（per）8=J以95%置信区间和均数±标准差呈现
			9 毛囊密度 个/mm² Density of hair follicles 9=K/(A-F)
			10皮脂腺密度 个/mm² Density of Sebaceous glands 10=M/(A-F)
			11 皮脂腺面积占比 % Sebaceous glands area% 11=L/(A-F)运算前注意统一单位
			12 毛囊面积占比 % Hair follicles area% 12=O/(A-F)
			13 皮肤面积 mm² Skin area1 3=A-F
		 */
		//BigDecimal A_sub_F = A_37A111_area.subtract(F_37A07A_area);
		// 6 表皮角质层面积占比 % Stratum corneum area% 6=H/(A-F)
		map.put("表皮角质层面积占比", createNameIndicator("Stratum corneum area%", getProportion(H_37A096_area, N_37A0C3_area), PERCENTAGE, "37A096,37A0C3"));

		// 7 表皮颗粒层+棘层+基底细胞层面积占比 % Nucleated cell layer area% 7=I/(A-F)
		map.put("表皮基底层+棘层+颗粒层面积占比", createNameIndicator("Nucleated cell layer area%", getProportion(I_37A097_area, N_37A0C3_area), PERCENTAGE, "37A097,37A0C3"));
		// 8 毛囊面积（单个）×10³ μm2 Hair follicle area（per）8=J以95%置信区间和均数±标准差呈现
		List<Annotation> skinStructureContourList = commonJsonParser.getStructureContourList(jsonTask, "37A098");
		List<BigDecimal> skinLists = new ArrayList<>();
		if (CollectionUtil.isNotEmpty(skinStructureContourList)) {
			for (Annotation annotation : skinStructureContourList) {
				// 默认平方毫米 转 103平方微米
				BigDecimal areaNum = annotation.getStructureAreaNum().multiply(new BigDecimal(1000));
				skinLists.add(areaNum);
			}
		}
		map.put("毛囊面积（单个）", createNameIndicator("Hair follicle area（per）", MathUtils.getConfidenceInterval(skinLists), MULTIPLIED_SQ_UM_THOUSAND, "37A098"));

		// 9 毛囊密度 个/mm² Density of hair follicles
		map.put("毛囊密度", createNameIndicator("Density of hair follicles", getProportion(new BigDecimal(K_37A098_count), N_37A0C3_area), SQ_MM_PIECE, "37A098,37A0C3"));

		// 10皮脂腺密度 个/mm² Density of Sebaceous glands
		map.put("皮脂腺密度", createNameIndicator("Density of Sebaceous glands", getProportion(new BigDecimal(M_37A099_count), N_37A0C3_area), SQ_MM_PIECE,  "37A099,37A0C3"));

		// 11 皮脂腺面积占比 % Sebaceous glands area% 11=L/(A-F)运算前注意统一单位
		map.put("皮脂腺面积占比", createNameIndicator("Sebaceous glands area%", getProportion(L_37A099_area, N_37A0C3_area), PERCENTAGE,  "37A099,37A0C3"));

		// 12 毛囊面积占比 % Hair follicles area% 12=O/(A-F)
		map.put("毛囊面积占比", createNameIndicator("Hair follicles area%", getProportion(O_37A098_area, N_37A0C3_area), PERCENTAGE,  "37A098,37A0C3"));

		// 13 皮肤面积 mm² Skin area1 3=A-F
		map.put("皮肤面积", createNameIndicator("Skin area1", N_37A0C3_area, SQ_MM, "37A0C3"));

		aiForecastService.addAiForecast(jsonTask.getSingleId(), map);

		log.info("指标计算结束-皮肤（乳腺附近）及乳腺");
	}

	@Override
	public String getAlgorithmCode() {
		return "Skin_with_mammary_gland_3";
	}
}
