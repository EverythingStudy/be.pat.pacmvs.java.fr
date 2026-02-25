package cn.staitech.fr.service.strategy.json.impl.dog.urology;

import cn.hutool.core.util.ObjectUtil;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.IndicatorAddIn;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.service.strategy.json.AbstractCustomParserStrategy;
import cn.staitech.fr.service.strategy.json.CommonJsonCheck;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @ClassName: UrinaryBladder_3ParserStrategyImpl
 * @Description:犬-膀胱
 * @author wanglibei
 * @date 2026年2月11日
 * @version V1.0
 */
@Slf4j
@Service("Urinary_bladder_3")
public class UrinaryBladder_3ParserStrategyImpl extends AbstractCustomParserStrategy {
	@Resource
	private AiForecastService aiForecastService;
	@Resource
	private CommonJsonParser commonJsonParser;
	@Resource
	private AreaUtils areaUtils;
	@Resource
	private CommonJsonCheck commonJsonCheck;
	@Resource
	private SingleSlideMapper singleSlideMapper;

	@PostConstruct
	public void init() {
		setCommonJsonParser(commonJsonParser);
		setCommonJsonCheck(commonJsonCheck);
		log.info("UrinaryBladderParserStrategyImpl init");
	}

	@Override
	public void alculationIndicators(JsonTask jsonTask) {
		log.info("UrinaryBladderParserStrategyImpl start");
		/**
		 * 
        黏膜上皮 31E035
        黏膜上皮细胞核 31E036
        血管 31E003
        组织轮廓 31E111
		 */

		try {
			/**
			 * 
        	算法输出指标指标代码（仅限本文档）单位（保留小数点后三位）备注相关结构
        	黏膜上皮面积 A mm2 31E035
        	黏膜上皮细胞核数量  B 个 数据相加输出 31E036
        	血管面积 C mm2 数据相加输出 31E003
        	组织轮廓面积 F mm2 仅辅助指标6计算，数值不显示在页面指标表格里 31E111
			 */

			// A  黏膜上皮面积
			BigDecimal A_31E035_area = getOrganArea(jsonTask, "31E035").getStructureAreaNum();
			// B 黏膜上皮细胞核数量
			Integer B_31E036_count = getOrganAreaCount(jsonTask, "31E036");
			// C 血管面积
			BigDecimal C_31E003_area = getOrganArea(jsonTask, "31E003").getStructureAreaNum();
			//F 组织轮廓
			SingleSlide singleSlide = singleSlideMapper.selectById(jsonTask.getSingleId());
			BigDecimal F_31E111 = new BigDecimal(0);
			if (ObjectUtil.isNotEmpty(singleSlide) && StringUtils.isNotEmpty(singleSlide.getArea())) {
				F_31E111 = F_31E111.add(new BigDecimal(singleSlide.getArea()));
			}

			Map<String, IndicatorAddIn> resultsMap = new HashMap<>();

			// 算法输出指标
			/**
			 * 
        	算法输出指标指标代码（仅限本文档）单位（保留小数点后三位）备注相关结构

        	黏膜上皮面积 A mm2 31E035
        	黏膜上皮细胞核数量  B 个 数据相加输出 31E036
        	血管面积 C mm2 数据相加输出 31E003
        	组织轮廓面积 F mm2 仅辅助指标6计算，数值不显示在页面指标表格里 31E111
			 */
			resultsMap.put("黏膜上皮面积", createIndicator(A_31E035_area.setScale(3, RoundingMode.HALF_UP).toString(), SQ_MM, "31E035"));
			resultsMap.put("黏膜上皮细胞核数量", createIndicator(B_31E036_count, PIECE, "31E035"));
			resultsMap.put("血管面积", createIndicator(C_31E003_area.setScale(3, RoundingMode.UP), SQ_MM, "31E003"));
			resultsMap.put("组织轮廓面积", createIndicator(F_31E111.setScale(3, RoundingMode.UP), SQ_MM, "31E111"));

			/**
			 * 
    		产品呈现指标指标代码（仅限本文档）单位（保留小数点后三位）English计算方式备注

    		黏膜上皮面积占比 1 % Mucosa epithelium area % 1=A/F
    		黏膜上皮细胞核密度 2 个/mm2 Nucleus density of mucosal epithelial nucleus 2=B/A
    		血管面积占比3 % Vessel area % 3=C/F
    		膀胱面积 6 mm2 Urinary bladder area 6=F
			 */

			// 计算指标
			//黏膜上皮面积占比
			BigDecimal mucosaEpitheliumArea = getProportion(A_31E035_area, F_31E111);
			// 黏膜上皮细胞核密度
			BigDecimal nucleusDensityOfMucosalEpithelialNucleus = getProportion(new BigDecimal(B_31E036_count), A_31E035_area);
			// 血管面积占比
			BigDecimal vesselArea = getProportion(C_31E003_area, F_31E111);
			// 膀胱面积 F


			// 产品呈现指标
			resultsMap.put("黏膜上皮面积占比", createNameIndicator("Mucosa epithelium area %", mucosaEpitheliumArea, PERCENTAGE, "31E035,31E111"));
			resultsMap.put("黏膜上皮细胞核密度", createNameIndicator("Nucleus density of mucosal epithelial nucleus", nucleusDensityOfMucosalEpithelialNucleus, SQ_MM_PIECE, "31E036,31E035"));
			resultsMap.put("血管面积占比", createNameIndicator("Vessel area %", vesselArea, PERCENTAGE, "31E003,31E111"));
			resultsMap.put("膀胱面积", createNameIndicator("Urinary bladder area", F_31E111, SQ_MM, "31E111"));
			aiForecastService.addAiForecast(jsonTask.getSingleId(), resultsMap);
		} catch (Exception e) {
			log.info("UrinaryBladderParserStrategyImpl start-2:{}", e);
			e.printStackTrace();
		}
		log.info("UrinaryBladderParserStrategyImpl end");
	}

	@Override
	public String getAlgorithmCode() {
		return "Urinary_bladder_3";
	}
}
