package cn.staitech.fr.service.strategy.json.impl.dog.endocrinology;

import cn.hutool.core.collection.CollectionUtil;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.DynamicData;
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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 
* @ClassName: ThyroidGlandParserStrategyImpl
* @Description:犬-甲状腺与甲状旁腺
* @author wanglibei
* @date 2026年2月11日
* @version V1.0
 */
@Slf4j
@Component("Thyroid_glands_with_parathyroids_3")
public class TgPd_3ParserStrategyImpl extends AbstractCustomParserStrategy {
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
    @Resource
    private AreaUtils areaUtils;
    @Resource
    private SingleSlideService singleSlideService;

    @PostConstruct
    public void init() {
        setCommonJsonParser(commonJsonParser);
        setCommonJsonCheck(commonJsonCheck);
        log.debug("TgPd_3ParserStrategyImpl init");
    }

    @Override
    public void alculationIndicators(JsonTask jsonTask) {
        log.info("指标计算开始-甲状腺与甲状旁腺");
        /**
         * 
        
        脏器结构编码
        甲状腺
        甲状腺组织轮廓 37F07C
        甲状腺滤泡 37F088
        甲状腺滤泡腔 37F08A
        血管 37F003
        红细胞 37F004
        滤泡上皮细胞核 37F089
        C细胞核 37F08F
        C细胞团 37F08B
        
        甲状旁腺
        甲状旁腺组织轮廓 37F07D
        甲状旁腺细胞核 37F092
         
        
        脏器 算法输出指标 指标代码（仅限本文档）单位（保留小数点后三位）备注 相关结构
        
        甲状腺
        甲状腺滤泡面积（单个）A ×10³ μm² 显示在单个甲状腺滤泡轮廓弹窗中，不显示在指标表格里 37F088
        甲状腺滤泡腔面积（单个）B ×10³ μm² 若单个甲状腺滤泡内有多个滤泡腔，则相加输出显示在单个甲状腺滤泡轮廓弹窗中，不显示在指标表格里 37F08A
        甲状腺滤泡面积（全片）C mm² 37F088
        血管面积D ×10³ μm² 数据相加输出 37F003
        血管内红细胞面积E ×10³ μm² 数据相加输出 37F003、37F004
        血管外红细胞面积F ×10³ μm² 数据相加输出 37F003、37F004
        滤泡上皮细胞核数量（单个）G 个 单个甲状腺滤泡内细胞核数量显示在单个甲状腺滤泡轮廓弹窗中，不显示在指标表格里 37F088、37F089
        甲状腺组织轮廓面积 H mm² 数据相加输出仅辅助指标9计算，数值不显示在页面指标表格里 37F07C
        C细胞核数量 I 个 数据相加输出 37F08F
        C细胞团面积 J mm² 数据相加输出 37F08B
        
        甲状旁腺
        甲状旁腺细胞核数量 K 个 数据相加输出 37F092
		甲状旁腺组织轮廓面积 L ×10³ μm² 若多个数据则相加输出仅辅助指标13计算，数值不显示在页面指标表格里 37F07D
        */
        
        
        // A 滤泡面积（单个）
        Annotation annotationA = new Annotation();
        annotationA.setAreaName("甲状腺滤泡面积（单个）");
        annotationA.setAreaUnit(MULTIPLIED_SQ_UM_THOUSAND);
        commonJsonParser.putSingleAnnotationDynamicData(jsonTask, "37F088", annotationA, 1);
        // B 滤泡腔面积（单个）
        Annotation annotationByB = new Annotation();
        annotationByB.setAreaName("甲状腺滤泡腔面积（单个）");
        annotationByB.setAreaUnit(MULTIPLIED_SQ_UM_THOUSAND);
        commonJsonParser.putAnnotationDynamicData(jsonTask, "37F088", "37F08A", annotationByB, 1, true);
        Annotation annotationC = new Annotation();
        annotationC.setAreaName("甲状腺滤泡上皮面积占比（单个）");
        annotationC.setAreaUnit(PERCENTAGE);
        putAnnotationDynamicData(jsonTask, annotationC);
        
        //C 甲状腺滤泡面积（全片） mm² 37F088
        BigDecimal C_37F088_area = commonJsonParser.getOrganArea(jsonTask, "37F088").getStructureAreaNum();
        
        // D 血管面积		10³平方微米	若多个数据则相加输出
        BigDecimal D_37F003_Micron_area = getOrganAreaMicron(jsonTask, "37F003");
        BigDecimal D_37F003_area = getOrganArea(jsonTask, "37F003").getStructureAreaNum();
        // E 血管内红细胞面积	 ×10³ μm² 数据相加输出 37F003、37F004       	平方微米	若多个数据则相加输出 (查询血管内红细胞面积)
        BigDecimal E_37F003_37F004_area = getInsideOrOutside(jsonTask, "37F003", "37F004", true).getStructureAreaNum();
        String E_37F003_37F004_areaStr = "";
        if(null != E_37F003_37F004_area) {
        	//源数据 E_37F003_37F004_area单位是平方毫米，需要转成10³ μm²
        	 E_37F003_37F004_areaStr =  commonJsonParser.convertToSquareMicrometer(E_37F003_37F004_area.toPlainString());
        }
        // F 血管外红细胞面积	F	平方微米	若多个数据则相加输出 (查询血管外红细胞面积)
        BigDecimal F_37F003_37F004_area = getInsideOrOutside(jsonTask, "37F003", "37F004", false).getStructureAreaNum();
        String F_37F003_37F004_areaStr = "";
        if(null != F_37F003_37F004_area) {
        	//源数据 F_37F003_37F004_areaStr单位是平方毫米，需要转成10³ μm²
        	F_37F003_37F004_areaStr =  commonJsonParser.convertToSquareMicrometer(F_37F003_37F004_area.toPlainString());
        }
        
       //G 滤泡上皮细胞核数量（单个） 个 单个甲状腺滤泡内细胞核数量显示在单个甲状腺滤泡轮廓弹窗中，不显示在指标表格里 37F088、37F089    
        Annotation annotationByH = new Annotation();
        annotationByH.setCountName("滤泡上皮细胞核数量（单个）");
        annotationByH.setCountUnit("个");
        commonJsonParser.putAnnotationDynamicData(jsonTask, "37F088", "37F089", annotationByH);
        
       //H 甲状腺组织轮廓面积  mm² 数据相加输出仅辅助指标9计算，数值不显示在页面指标表格里 37F07C
        SingleSlide singleSlideH = singleSlideService.getSingleSlide(jsonTask.getSingleId(), jsonTask.getImageId(), "37F07C");
        BigDecimal H_37F07C_area = BigDecimal.ZERO;
        if(null != singleSlideH) {
        	H_37F07C_area = new BigDecimal(singleSlideH.getArea());
        }
        
        //I C细胞核数量 个 数据相加输出 37F08F
        Integer I_37F08F_count = getOrganAreaCount(jsonTask, "37F08F");
        
        //J C细胞团面积  mm² 数据相加输出 37F08B
        BigDecimal J_37F08B_area = getOrganArea(jsonTask, "37F08B").getStructureAreaNum();
        
        //甲状旁腺
        //K 甲状旁腺细胞核数量  个 数据相加输出 37F092
        Integer K_37F092_count = commonJsonParser.getOrganAreaCount(jsonTask, "37F092");
        K_37F092_count = commonJsonParser.getIntegerValue(K_37F092_count);
        
		//L 甲状旁腺组织轮廓面积  ×10³ μm² 若多个数据则相加输出仅辅助指标13计算，数值不显示在页面指标表格里 37F07D
        SingleSlide singleSlideL = singleSlideService.getSingleSlide(jsonTask.getSingleId(), jsonTask.getImageId(), "37F07D");
        BigDecimal L_37F07D_area = BigDecimal.ZERO;
        if(null != singleSlideL) {
        	String L_areaStr = commonJsonParser.convertToSquareMicrometer(singleSlideL.getArea()); 
        	L_37F07D_area = new BigDecimal(L_areaStr);
        }
        
        // 计算置信区间和均数±标准差呈现  -------------------------------------------------------------
        // 1 甲状腺滤泡面积（单个）	1	10³平方微米	Thyroid follicle area (per)	1=A	以95%置信区间和均数±标准差呈现
        List<BigDecimal> list1 = new ArrayList<>();

        // 2 甲状腺滤泡腔面积（单个）	2	10³平方微米	Thyroid follicular lumen area (per)	2=B	以95%置信区间和均数±标准差呈现
        List<BigDecimal> list2 = new ArrayList<>();

        // 3 甲状腺滤泡上皮面积占比（单个）	3	%	Thyroid follicular epithelium area%(per)	3=(A-B)/A	以95%置信区间和均数±标准差呈现
        List<BigDecimal> list3 = new ArrayList<>();

        // 滤泡上皮细胞核密度（单个）	8	个/10³平方微米	Nucleus density of follicular cell (per)	8=G/(A-B) 	以95%置信区间和均数±标准差呈现
        List<BigDecimal> list8 = new ArrayList<>();

        List<Annotation> structureContourList = getStructureContourList(jsonTask, "37F088");

        if (CollectionUtils.isNotEmpty(structureContourList)) {
            for (Annotation annotation : structureContourList) {

                // A 甲状腺滤泡面积（单个）	A	10³平方微米	单个甲状腺滤泡（37F088）面积
                BigDecimal structureAreaNumA = annotation.getStructureAreaNum().multiply(new BigDecimal(1000));
                list1.add(structureAreaNumA);

                // 甲状腺滤泡内 滤泡腔
                Annotation contourInsideOrOutsideB = commonJsonParser.getContourInsideOrOutside(jsonTask, annotation.getContour(), "37F08A", true);
                // 甲状腺滤泡内 滤泡上皮细胞核
                Annotation contourInsideOrOutsideG = commonJsonParser.getContourInsideOrOutside(jsonTask, annotation.getContour(), "37F089", true);

                // B 甲状腺滤泡腔面积（单个）	B	10³平方微米	若单个甲状腺滤泡内有多个滤泡腔，则相加输出 (true在里面）
                BigDecimal structureAreaNumB = contourInsideOrOutsideB.getStructureAreaNum().multiply(new BigDecimal(1000));
                list2.add(structureAreaNumB);

                // A-B
                BigDecimal subtractAB = structureAreaNumA.subtract(structureAreaNumB);
                // C 3=(A-B)/A 甲状腺滤泡上皮面积占比（单个）	3	%	Thyroid follicular epithelium area%(per)	3=(A-B)/A	以95%置信区间和均数±标准差呈现
                if (structureAreaNumA.compareTo(BigDecimal.ZERO) != 0) {
                    list3.add(subtractAB.divide(structureAreaNumA, 7, RoundingMode.HALF_UP).multiply(new BigDecimal(100)));

                }
                // H 滤泡上皮细胞核数量（单个）	H	个	单个甲状腺滤泡内细胞核数量
                Integer count = contourInsideOrOutsideG.getCount();

                // 8 滤泡上皮细胞核密度（单个）	8	个/10³平方微米	Nucleus density of follicular cell (per)	8=H/(A-B) 	以95%置信区间和均数±标准差呈现
                if (subtractAB.compareTo(BigDecimal.ZERO) != 0) {
                    list8.add(new BigDecimal(count).divide(subtractAB, 7, RoundingMode.HALF_UP));
                }
            }
        }

        /**
         * 
        
        脏器 算法输出指标 指标代码（仅限本文档）单位（保留小数点后三位）备注 相关结构
        
        甲状腺
        甲状腺滤泡面积（单个）A ×10³ μm² 显示在单个甲状腺滤泡轮廓弹窗中，不显示在指标表格里 37F088
        甲状腺滤泡腔面积（单个）B ×10³ μm² 若单个甲状腺滤泡内有多个滤泡腔，则相加输出显示在单个甲状腺滤泡轮廓弹窗中，不显示在指标表格里 37F08A
        甲状腺滤泡面积（全片）C mm² 37F088
        血管面积D ×10³ μm² 数据相加输出 37F003
        血管内红细胞面积E ×10³ μm² 数据相加输出 37F003、37F004
        血管外红细胞面积F ×10³ μm² 数据相加输出 37F003、37F004
        滤泡上皮细胞核数量（单个）G 个 单个甲状腺滤泡内细胞核数量显示在单个甲状腺滤泡轮廓弹窗中，不显示在指标表格里 37F088、37F089
        甲状腺组织轮廓面积 H mm² 数据相加输出仅辅助指标9计算，数值不显示在页面指标表格里 37F07C
        C细胞核数量 I 个 数据相加输出 37F08F
        C细胞团面积 J mm² 数据相加输出 37F08B
        
        甲状旁腺
        甲状旁腺细胞核数量 K 个 数据相加输出 37F092
		甲状旁腺组织轮廓面积 L ×10³ μm² 若多个数据则相加输出仅辅助指标13计算，数值不显示在页面指标表格里 37F07D
        */
        Map<String, IndicatorAddIn> map = new HashMap<>();
        //算法输出指标------------------------------------------------------------------------------------------
        //-----------甲状腺
        //C 甲状腺滤泡面积（全片） mm² 37F088
        map.put("甲状腺滤泡面积（全片）", createIndicator(C_37F088_area.toString(), SQ_MM, "37F088"));
        // D 血管面积 ×10³ μm² 数据相加输出 37F003
        map.put("血管面积", createIndicator(D_37F003_Micron_area.toString(), MULTIPLIED_SQ_UM_THOUSAND, "37F003"));
        // E  血管内红细胞面积×10³ μm² 数据相加输出 37F003、37F004
        map.put("血管内红细胞面积", createIndicator(E_37F003_37F004_areaStr, MULTIPLIED_SQ_UM_THOUSAND, "37F003,37F004"));
        // F  血管外红细胞面积 ×10³ μm² 数据相加输出 37F003、37F004
        map.put("血管外红细胞面积", createIndicator(F_37F003_37F004_areaStr, MULTIPLIED_SQ_UM_THOUSAND, "37F003,37F004"));
        //H 甲状腺组织轮廓面积  mm² 数据相加输出仅辅助指标9计算，数值不显示在页面指标表格里 37F07C
//        map.put("甲状腺组织轮廓面积", createIndicator(H_37F07C_area, SQ_MM, "37F07C"));
        //I  C细胞核数量  个 数据相加输出 37F08F
        map.put("C细胞核数量", createIndicator(I_37F08F_count, PIECE, "37F08F"));
        //J  C细胞团面积  mm² 数据相加输出 37F08B
        map.put("C细胞团面积", createIndicator(J_37F08B_area, SQ_MM, "37F08B"));
        
       //-----------甲状旁腺
        //甲状旁腺细胞核数量 K 个 数据相加输出 37F092
        map.put("甲状旁腺细胞核数量", createIndicator(K_37F092_count, PIECE, "37F092"));
        //甲状旁腺组织轮廓面积 L ×10³ μm² 若多个数据则相加输出仅辅助指标13计算，数值不显示在页面指标表格里 37F07D
//        map.put("甲状旁腺组织轮廓面积", createIndicator(L_37F07D_area.toString(), MULTIPLIED_SQ_UM_THOUSAND, "37F07D"));
        
        // 产品呈现指标 -------------------------------------------------------------
        /**
         * 
         *  脏器 产品呈现指标 指标代码（仅限本文档）单位（保留小数点后三位）English 计算方式 备注
			甲状腺
			甲状腺滤泡面积（单个）1 ×10³ μm² Thyroid follicle area (per) 1=A以95%置信区间和均数±标准差呈现
			甲状腺滤泡腔面积（单个）2 ×10³ μm²Thyroid follicular lumen area (per) 2=B以95%置信区间和均数±标准差呈现
			甲状腺滤泡上皮面积占比（单个）3 % Thyroid follicular epithelium area%(per) 3=(A-B)/A以95%置信区间和均数±标准差呈现
			甲状腺血管面积占比 4 % Vessel area% 4=D/H运算前注意统一单位
			甲状腺血管内红细胞面积占比 5 % Intravascular erythrocyte area% 5=E/H运算前注意统一单位
			甲状腺血管外红细胞面积占比 6 % Extravascular erythrocyte area% 6=F/H运算前注意统一单位
			甲状腺滤泡上皮细胞核密度（单个）7 个/10³ μm²Nucleus density of follicular cell (per) 7=G/(A-B) 以95%置信区间和均数±标准差呈现
			甲状腺滤泡面积占比 8 % Thyroid follicle area% 8=C/H
			甲状腺面积 9 mm² Thyroid gland area 9=H
			甲状腺C细胞核密度 10 个/mm² Nucleus density of  C cell 10=I/H
			甲状腺C细胞团面积占比 11 % Nuclear density of C-cell cluster 11=J/H
			
			甲状旁腺
			甲状旁腺细胞核密度12 个/10³ μm² Nucleus density of chief cell 12=K/L运算前注意统一单位
			甲状旁腺面积13 ×10³ μm² Parathyroid gland area 13=L
         */
        
        // 1 甲状腺滤泡面积（单个） ×10³ μm² Thyroid follicle area (per) 1=A以95%置信区间和均数±标准差呈现
        map.put("甲状腺滤泡面积（单个）", createNameIndicator("Thyroid follicle area (per)", MathUtils.getConfidenceInterval(list1), MULTIPLIED_SQ_UM_THOUSAND, "37F088"));
        // 2 甲状腺滤泡腔面积（单个） ×10³ μm² Thyroid follicular lumen area (per) 2=B以95%置信区间和均数±标准差呈现
        map.put("甲状腺滤泡腔面积（单个）", createNameIndicator("Thyroid follicular lumen area (per)", MathUtils.getConfidenceInterval(list2), MULTIPLIED_SQ_UM_THOUSAND, "37F088,37F08A"));
        // 3 甲状腺滤泡上皮面积占比（单个） %   Thyroid follicular epithelium area%(per) 3=(A-B)/A以95%置信区间和均数±标准差呈现
        map.put("甲状腺滤泡上皮面积占比（单个）", createNameIndicator("Thyroid follicular epithelium area%(per)", MathUtils.getConfidenceInterval(list3), PERCENTAGE, "37F088,37F08A"));

        if (H_37F07C_area.compareTo(BigDecimal.ZERO) != 0) {
        	// 4 甲状腺血管面积占比   % Vessel area % 4=D/H 运算前注意统一单位   D 10³平方微米 H平方微米
            BigDecimal vesselAreaRate = getProportion(D_37F003_area, H_37F07C_area); 
            map.put("甲状腺血管面积占比", createNameIndicator("Vessel area", vesselAreaRate, PERCENTAGE, "37F003"));

            //5 甲状腺血管内红细胞面积占比  %  Intravascular erythrocyte area% 5=E/H运算前注意统一单位
            map.put("甲状腺血管内红细胞面积占比", createNameIndicator("Intravascular erythrocyte area%", getProportion(E_37F003_37F004_area, H_37F07C_area), PERCENTAGE, "37F003,37F004"));

            // 6 甲状腺血管外红细胞面积占比 %  Extravascular erythrocyte area% 6=F/H运算前注意统一单位
            map.put("甲状腺血管外红细胞面积占比", createNameIndicator("Extravascular erythrocyte area%", getProportion(F_37F003_37F004_area, H_37F07C_area), PERCENTAGE, "37F003,37F004"));

        } else {
            log.info("singleId{}组织轮廓面积为0", jsonTask.getSingleId());
        }
        // 8 甲状腺滤泡面积占比  % Thyroid follicle area% 8=C/H
        map.put("甲状腺滤泡面积占比", createNameIndicator("Thyroid follicle area%", getProportion(C_37F088_area, H_37F07C_area), PERCENTAGE, "37F088"));

        // 9 甲状腺面积 mm² Thyroid gland area 9=H
        map.put("甲状腺面积", createNameIndicator("Thyroid gland area", DecimalUtils.setScale3(H_37F07C_area), SQ_MM, null));
        
        //10 甲状腺C细胞核密度  个/mm² Nucleus density of  C cell 10=I/H
        map.put("甲状腺C细胞核密度", createNameIndicator("Nucleus density of  C cell", getProportion(new BigDecimal(I_37F08F_count) , H_37F07C_area), SQ_MM_PIECE, "37F08F"));

        //11 甲状腺C细胞团面积占比  % Nuclear density of C-cell cluster 11=J/H
        map.put("甲状腺C细胞团面积占比", createNameIndicator("Nuclear density of C-cell cluster", getProportion(J_37F08B_area, H_37F07C_area), PERCENTAGE, "37F08B"));
        
        //甲状旁腺
        //12 甲状旁腺细胞核密度 个/10³ μm² Nucleus density of chief cell 12=K/L运算前注意统一单位
        map.put("甲状旁腺细胞核密度", createNameIndicator("Nucleus density of chief cell", getProportion(new BigDecimal(K_37F092_count) , L_37F07D_area), SQ_UM_PICE, "37F092"));
        
        //13 甲状旁腺面积 ×10³ μm² Parathyroid gland area 13=L
        map.put("甲状旁腺面积", createNameIndicator("Parathyroid gland area", DecimalUtils.setScale3(L_37F07D_area), MULTIPLIED_SQ_UM_THOUSAND, null));
        
        
        //  甲状腺滤泡上皮细胞核密度（单个）	个/10³平方微米	Nucleus density of follicular cell (per)	8=H/(A-B) 	以95%置信区间和均数±标准差呈现
        //map.put("甲状腺滤泡上皮细胞核密度（单个）", createNameIndicator("Nucleus density of follicular cell (per)", MathUtils.getConfidenceInterval(list8), SQ_UM_THOUSAND, "37F088,37F089,37F08A"));

        aiForecastService.addAiForecast(jsonTask.getSingleId(), map);
        
        
        log.info("指标计算结束-甲状腺与甲状旁腺");
    }

    @Override
    public String getAlgorithmCode() {
        return "Thyroid_glands_with_parathyroids_3";
    }

    private void putAnnotationDynamicData(JsonTask jsonTask, Annotation annotation) {
        Long sequenceNumber = commonJsonParser.getSequenceNumber(jsonTask.getSpecialId());
        List<Annotation> annotationList = getStructureContourList(jsonTask, "37F088");

        if (CollectionUtil.isEmpty(annotationList)) {
            return;
        }

        // 批量处理数据
        List<Annotation> batchUpdates = new ArrayList<>(annotationList.size());

        // 预处理通用数据
        boolean hasAreaName = annotation.getAreaName() != null;

        String areaUnit = annotation.getAreaUnit();

        for (Annotation item : annotationList) {
            Annotation annotationBy = getContourInsideOrOutside(jsonTask, item.getContour(), "37F08A", true);

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
                dynamicData.setData(String.valueOf(getProportion(item.getStructureAreaNum().subtract(annotationBy.getStructureAreaNum()), item.getStructureAreaNum())));
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
            if (!jsonArray.isEmpty()) {
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

}
