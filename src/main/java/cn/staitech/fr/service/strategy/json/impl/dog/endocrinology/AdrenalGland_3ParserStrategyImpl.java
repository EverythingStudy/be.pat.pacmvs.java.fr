package cn.staitech.fr.service.strategy.json.impl.dog.endocrinology;

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
import java.util.Map;

/**
 * 
 * @ClassName: AdrenalGlandParserStrategyImpl
 * @Description:犬-肾上腺
 * @author wanglibei
 * @date 2026年2月11日
 * @version V1.0
 */
@Slf4j
@Service("Adrenal_glands_3")
public class AdrenalGland_3ParserStrategyImpl extends AbstractCustomParserStrategy {
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
		log.debug("AdrenalGland_3ParserStrategyImpl init");
	}

	@Override
	public void alculationIndicators(JsonTask jsonTask) {
		/**
		 * 
    	结构编码
    	皮质30103D
    	球状带301056
    	束状带+网状带301057
    	髓质30103E
    	实质细胞核301068
    	红细胞301004
    	组织轮廓301111
		 */

		/**
		 * 
        算法输出指标指标代码（仅限本文档）单位（保留小数点后三位）备注相关结构
        皮质面积 A mm² 30103D
        髓质面积 B mm² 30103E
        球状带面积 C mm² 扣减后输出 301056
        束状带+网状带面积 D mm² 扣减后输出 301057
        球状带细胞核数量 E 个 包含在球状带轮廓内的所有实质细胞核轮廓数量 301056、301068
        束状带+网状带细胞核数量 F 个 包含在束状带+网状带轮廓内的所有实质细胞核轮廓数量 301057、301068
        髓质细胞核数量 G 个 包含在髓质轮廓内的所有实质细胞核轮廓数量 30103E、301068
        皮质红细胞面积 H mm² 30103D、301004
        髓质红细胞面积 I mm² 30103E、301004
        组织轮廓面积 J mm² 仅辅助指标11计算，数值不显示在页面指标表格里 301111
		 */

		//A 皮质
		BigDecimal A_30103D_area = getOrganArea(jsonTask, "30103D").getStructureAreaNum();
		//B 髓质
		BigDecimal B_30103E_area = getOrganArea(jsonTask, "30103E").getStructureAreaNum();
		//C球状带面积 C mm² 扣减后输出 301056
		BigDecimal C_301056_area = getOrganArea(jsonTask, "301056").getStructureAreaNum();
		//D 束状带+网状带面积 D mm² 扣减后输出 301057
		BigDecimal D_301057_area = getOrganArea(jsonTask, "301057").getStructureAreaNum();
		//E 球状带细胞核数量  个 包含在球状带轮廓内的所有实质细胞核轮廓数量 301056、301068
//		Integer E_301056_301068_count = getInsideOrOutside(jsonTask, "301056", "301068", true).getCount();
		//F 束状带+网状带细胞核数量  个 包含在束状带+网状带轮廓内的所有实质细胞核轮廓数量 301057、301068
//		Integer F_301057_301068_count = getInsideOrOutside(jsonTask, "301057", "301068", true).getCount();
		//G 髓质细胞核数量 包含在髓质轮廓内的所有实质细胞核轮廓数量 30103E、301068
//		Integer G_30103E_301068_count = getInsideOrOutside(jsonTask, "30103E", "301068", true).getCount();
		//参考肺脏 所有血管轮廓内的红细胞轮廓面积之和
		//      BigDecimal intravascularErythrocyteArea = commonJsonParser.getInsideOrOutside(jsonTask, "14C003", "14C004", true).getStructureAreaNum();
		//参考肺脏 所有血管轮廓外的红细胞轮廓面积之和
		//      BigDecimal extravascularErythrocyteArea = commonJsonParser.getInsideOrOutside(jsonTask, "14C003", "14C004", false).getStructureAreaNum();
		//H 皮质红细胞面积  30103D、301004
		BigDecimal H_30103D_301004_area = commonJsonParser.getInsideOrOutside(jsonTask, "30103D", "301004", true).getStructureAreaNum();
		//I 髓质红细胞面积  mm² 30103E、301004
		BigDecimal I_30103E_301004_area = commonJsonParser.getInsideOrOutside(jsonTask, "30103E", "301004", true).getStructureAreaNum();

		//J 组织轮廓面积 平方毫米	若多个数据则相加输出
		SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
		String accurateArea = singleSlide.getArea();
		BigDecimal J_301111_area = new BigDecimal(accurateArea);

		/**
		 * 
        算法输出指标指标代码（仅限本文档）单位（保留小数点后三位）备注相关结构
        皮质面积 A mm² 30103D
        髓质面积 B mm² 30103E
        球状带面积 C mm² 扣减后输出 301056
        束状带+网状带面积 D mm² 扣减后输出 301057
        球状带细胞核数量 E 个 包含在球状带轮廓内的所有实质细胞核轮廓数量 301056、301068
        束状带+网状带细胞核数量 F 个 包含在束状带+网状带轮廓内的所有实质细胞核轮廓数量 301057、301068
        髓质细胞核数量 G 个 包含在髓质轮廓内的所有实质细胞核轮廓数量 30103E、301068
        皮质红细胞面积 H mm² 30103D、301004
        髓质红细胞面积 I mm² 30103E、301004
        组织轮廓面积 J mm² 仅辅助指标11计算，数值不显示在页面指标表格里 301111
		 */
		Map<String, IndicatorAddIn> indicatorResultsMap = new HashMap<>();
		indicatorResultsMap.put("皮质面积", createIndicator(A_30103D_area.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "30103D"));
		indicatorResultsMap.put("髓质面积", createIndicator(B_30103E_area.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "30103E"));
		indicatorResultsMap.put("球状带面积", createIndicator(C_301056_area.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "301056"));
		indicatorResultsMap.put("束状带+网状带面积", createIndicator(D_301057_area.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "301057"));
//		indicatorResultsMap.put("球状带细胞核数量", createIndicator(E_301056_301068_count, PIECE, "301057"));
//		indicatorResultsMap.put("束状带+网状带细胞核数量", createIndicator(F_301057_301068_count, PIECE, "301057"));
//		indicatorResultsMap.put("髓质细胞核数量", createIndicator(G_30103E_301068_count, PIECE, "30103E,301068"));
		indicatorResultsMap.put("皮质红细胞面积", createIndicator(H_30103D_301004_area.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "30103D,301004"));
		indicatorResultsMap.put("髓质红细胞面积", createIndicator(I_30103E_301004_area.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "30103E,301004"));
//		indicatorResultsMap.put("组织轮廓面积", createIndicator(J_301111_area, SQ_MM, "301111"));
		/**
		 * 
        产品呈现指标指标代码（仅限本文档）单位(保留小数点后三位)English计算方式备注
        皮质面积占比 1 % Cortex area % 1=A/J
        髓质面积占比 2 % Medulla area% 2=B/J
        皮髓比 3 Cortex:Medulla ratio 3=A/B换算成“xxx : 1”的形式展示
        球状带面积占比 4 % Zona glomerulosa area% 4=C/J
        束状带+网状带面积占比 5 % Zona fasciculate and zona reticularis% 5=D/J
        球状带细胞核密度 6 个/mm² Nucleus density of zona glomerulosa 6=E/C
        束状带+网状带细胞核密度 7 个/mm² Nucleus density of zona fasciculate and zona reticularis 7=F/D
        髓质细胞核密度 8 个/mm² Nucleus density of adrenal medulla 8=G/B
        皮质红细胞面积占比 9 % Cortial erythrocyte area% 9=H/J
        髓质红细胞面积占比 10 % Medullary erythrocyte area% 10=I/J
        肾上腺面积 11 mm² Adrenal gland area 11=J
		 */

		//皮质面积占比 1 % Cortex area % 1=A/J
		BigDecimal b1 = getProportion(A_30103D_area, J_301111_area);
		indicatorResultsMap.put("皮质面积占比", createNameIndicator("Cortex area %", String.valueOf(b1), PERCENTAGE, "30103D,301111"));
		//髓质面积占比 2 % Medulla area% 2=B/J
		BigDecimal b2 = commonJsonParser.getProportion(B_30103E_area, J_301111_area);
		indicatorResultsMap.put("髓质面积占比", createNameIndicator("Medulla area%", String.valueOf(b2), PERCENTAGE, "30103E,301111"));
		//皮髓比 3 Cortex:Medulla ratio 3=A/B换算成“xxx : 1”的形式展示
		BigDecimal b3 = bigDecimalDivideCheck(A_30103D_area, B_30103E_area);
		indicatorResultsMap.put("皮髓比", createNameIndicator("Cortex:Medulla ratio", String.valueOf(b3)+":1", "无", "30103D,30103E"));
		//球状带面积占比 4 % Zona glomerulosa area% 4=C/J
		BigDecimal b4 = bigDecimalDivideCheck(C_301056_area, J_301111_area);
		indicatorResultsMap.put("球状带面积占比", createNameIndicator("Zona glomerulosa area%", String.valueOf(b4), PERCENTAGE, "301056,301111"));
		//束状带+网状带面积占比 5 % Zona fasciculate and zona reticularis% 5=D/J
		BigDecimal b5 = bigDecimalDivideCheck(D_301057_area, J_301111_area);
		indicatorResultsMap.put("束状带+网状带面积占比", createNameIndicator("Zona fasciculate and zona reticularis%", String.valueOf(b5), PERCENTAGE, "301057,301111"));
		//球状带细胞核密度 6 个/mm² Nucleus density of zona glomerulosa 6=E/C
//        BigDecimal b6 = bigDecimalDivideCheck(new BigDecimal(E_301056_301068_count), C_301056_area);
//		indicatorResultsMap.put("球状带细胞核密度", createNameIndicator("Nucleus density of zona glomerulosa", String.valueOf(b6), SQ_MM_PIECE, "301056,301068"));
		// 束状带+网状带细胞核密度 7 个/mm² Nucleus density of zona fasciculate and zona reticularis 7=F/D
//        BigDecimal b7 = bigDecimalDivideCheck(new BigDecimal(F_301057_301068_count), D_301057_area);
//		indicatorResultsMap.put("束状带+网状带细胞核密度", createNameIndicator("Nucleus density of zona fasciculate and zona reticularis", String.valueOf(b7), SQ_MM_PIECE, "301057,301068"));
		//髓质细胞核密度 8 个/mm² Nucleus density of adrenal medulla 8=G/B
//		BigDecimal b8 = bigDecimalDivideCheck(new BigDecimal(G_30103E_301068_count), B_30103E_area);
//		indicatorResultsMap.put("髓质细胞核密度", createNameIndicator("Nucleus density of zona fasciculate and zona reticularis", String.valueOf(b8), SQ_MM_PIECE, "30103E,30103E,301068"));
		//皮质红细胞面积占比 9 % Cortial erythrocyte area% 9=H/J
		BigDecimal b9 = bigDecimalDivideCheck(H_30103D_301004_area, J_301111_area);
		indicatorResultsMap.put("皮质红细胞面积占比", createNameIndicator("Zona fasciculate and zona reticularis%", String.valueOf(b9), PERCENTAGE, "301057,301111"));
		
		//髓质红细胞面积占比 10 % Medullary erythrocyte area% 10=I/J
		BigDecimal b10 = bigDecimalDivideCheck(I_30103E_301004_area, J_301111_area);
		indicatorResultsMap.put("髓质红细胞面积占比", createNameIndicator("Medullary erythrocyte area%", String.valueOf(b10), PERCENTAGE, "30103,301004,301111"));
		
		//肾上腺面积 11 mm² Adrenal gland area 11=J
		indicatorResultsMap.put("肾上腺面积", createNameIndicator("Adrenal gland area", J_301111_area, PERCENTAGE, "301111"));
		
		aiForecastService.addAiForecast(jsonTask.getSingleId(), indicatorResultsMap);
	}

	@Override
	public String getAlgorithmCode() {
		return "Adrenal_glands_3";
	}
}
