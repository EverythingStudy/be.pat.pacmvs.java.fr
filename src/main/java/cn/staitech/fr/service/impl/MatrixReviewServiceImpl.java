package cn.staitech.fr.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.AiForecast;
import cn.staitech.fr.domain.Project;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.dto.ExportAiDTO;
import cn.staitech.fr.domain.out.ExportAiListVO;
import cn.staitech.fr.domain.out.ExprotAiExcelEnVO;
import cn.staitech.fr.domain.out.ExprotAiExcelVO;
import cn.staitech.fr.mapper.AiForecastMapper;
import cn.staitech.fr.mapper.ProjectMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SlideMapper;
import cn.staitech.fr.service.MatrixReviewService;
import cn.staitech.fr.utils.ExportPdfUtils;
import cn.staitech.fr.utils.LanguageUtils;
import cn.staitech.fr.utils.MathUtils;
import cn.staitech.fr.vo.project.SlideDownLoadReq;
import cn.staitech.fr.vo.project.slide.SlidePageVo;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author wudi
 * @Date 2024/4/10 15:54
 * @desc
 */
@Service
@Slf4j
public class MatrixReviewServiceImpl implements MatrixReviewService {
    @Resource
    private SlideMapper slideMapper;
    @Autowired
    private ProjectMapper projectMapper;

    @Resource
    private SingleSlideMapper singleSlideMapper;

    @Resource
    private AiForecastMapper aiForecastMapper;


    @Value("${waxPath}")
    private String waxPath;
    @Resource
    private HttpServletResponse response;

    @Override
    public void algorithmDownload(SlideDownLoadReq req) throws Exception {
        log.info("ai预测报告导出接口开始：");
        //存放单脏器切片id和脏器id
        Map<Long, Long> categorys = new HashMap<>();
        // 显示的脏器
        List<Long> organTagIds = req.getOrganTagIds();
        //判断是不是存在对照组
        List<SingleSlide> singleSlides = new ArrayList<>();
        Project special = projectMapper.selectById(req.getProjectId());
        if (CollectionUtils.isEmpty(req.getSingleSlideIds())) {
            List<SlidePageVo> list = slideMapper.exportStructureList(req);
            singleSlides = singleSlideMapper.selectList(new LambdaQueryWrapper<>(SingleSlide.class).in(SingleSlide::getSlideId, list.stream().map(SlidePageVo::getSlideId).collect(Collectors.toList())));
        } else {
            singleSlides = singleSlideMapper.selectList(new LambdaQueryWrapper<>(SingleSlide.class).in(SingleSlide::getSingleId, req.getSingleSlideIds()));
        }
        List<Long> singleSlideIds = new ArrayList<>();
        for (SingleSlide singleSlide : singleSlides) {
            if (CollectionUtils.isEmpty(organTagIds) || organTagIds.contains(singleSlide.getCategoryId())) {
                singleSlideIds.add(singleSlide.getSingleId());
            }
        }
        //范围数据
        if (StringUtils.isEmpty(special.getControlGroup())) {
            special.setControlGroup("1");
        }
        categorys = singleSlides.stream().collect(Collectors.toMap(SingleSlide::getSingleId, SingleSlide::getCategoryId));
        List<ExprotAiExcelVO> collect = new ArrayList<>();
        for (Long id : singleSlideIds) {
            ExportAiDTO exportVO = singleSlideMapper.getExportSingleSlideInfoById(id);
            //算法结果数据填充
            LambdaQueryWrapper<AiForecast> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiForecast::getSingleSlideId, id);
            //wrapper.eq(AiForecast::getStructType, CommonConstant.NUMBER_0);
            List<AiForecast> aiForecasts = aiForecastMapper.selectList(wrapper);

            if (CollectionUtils.isNotEmpty(aiForecasts)) {
                for (AiForecast aiForecast : aiForecasts) {
                    ExportAiListVO exportAiListVO = new ExportAiListVO();
                    BeanUtils.copyProperties(aiForecast, exportAiListVO);
                    if (!CommonConstant.SINGLE_RESULT.equals(aiForecast.getResults()) && !(aiForecast.getResults().contains("±")) && new BigDecimal(aiForecast.getResults()).compareTo(BigDecimal.ZERO) < 0) {
                        exportAiListVO.setResults("?");
                    }
                    setReferenceScope(special, id, exportAiListVO, categorys);
                    ExprotAiExcelVO exportAiExcelVO = new ExprotAiExcelVO();
                    exportAiExcelVO.setQuantitativeIndicators(exportAiListVO.getQuantitativeIndicators());
                    exportAiExcelVO.setResults(exportAiListVO.getResults());
                    exportAiExcelVO.setUnit(exportAiListVO.getUnit());
                    exportAiExcelVO.setNormalDistribution(exportAiListVO.getNormalDistribution());
                    exportAiExcelVO.setSpecialName(exportVO.getSpecialName());
                    exportAiExcelVO.setTopicName(exportVO.getTopicName());
                    exportAiExcelVO.setImageName(exportVO.getImageName());
                    exportAiExcelVO.setOrganName(exportVO.getOrganName());
                    if (null != exportAiListVO.getNormalDistribution() && null != exportAiListVO.getResults() && !"数据量过少,无统计学意义".equals(exportAiListVO.getNormalDistribution())) {
                        String[] s = exportAiListVO.getNormalDistribution().split("-");
                        if (!"详情见单个标注轮廓详情弹窗！".equals(exportAiListVO.getResults()) && exportAiListVO.getResults().split(";").length == 1) {
                            try {
                                String result = exportAiListVO.getResults().contains("±")
                                        ? exportAiListVO.getResults().split("±")[0]
                                        : (exportAiListVO.getResults().contains(":") ? exportAiListVO.getResults().split(":")[0] : exportAiListVO.getResults());
                                boolean inRange = Range.between(new BigDecimal(s[0]), new BigDecimal(s[1])).contains(new BigDecimal(result));
                                exportAiExcelVO.setLowerBound(s[0]);
                                exportAiExcelVO.setUpperBound(s[1]);
                                if (!inRange) {
                                    exportAiExcelVO.setAbnormalValue("T");
                                }
                            } catch (Exception e) {
                                log.error("数据转换异常{},{}",  exportAiListVO.getNormalDistribution(), exportAiListVO.getResults());
                            }
                        } else {
                            exportAiExcelVO.setAbnormalValue("");
                        }
                    } else {
                        exportAiExcelVO.setLowerBound(exportAiListVO.getNormalDistribution());
                        exportAiExcelVO.setUpperBound(exportAiListVO.getNormalDistribution());
                    }
                    collect.add(exportAiExcelVO);
                }
            }

        }
        String s = waxPath + "AI" + File.separator + String.format("AI量化指标数据_%s_%s", special.getTopicName(), System.currentTimeMillis()) + CommonConstant.EXCEL_FILE;
        File file = new File(waxPath + "AI" + File.separator);
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
        }
        if(LanguageUtils.isEn()){
            List<ExprotAiExcelEnVO> excelModels = collect.stream()
                    .map(m -> {
                        ExprotAiExcelEnVO model = new ExprotAiExcelEnVO();
                        BeanUtils.copyProperties(m, model);
                        return model;
                    })
                    .collect(Collectors.toList());
            EasyExcel.write(s,ExprotAiExcelEnVO.class).sheet("AI量化指标数据").doWrite(excelModels);
        } else {
            EasyExcel.write(s, ExprotAiExcelVO.class).sheet("AI量化指标数据").doWrite(collect);
        }

        ExportPdfUtils.downloadLocal(s, response);
        log.info("结束");
    }


    /**
     * 设置参考范围
     *
     * @param special
     * @param singleId
     * @param exportAiListVO
     * @param categorys
     */
    private void setReferenceScope(Project special, Long singleId, ExportAiListVO exportAiListVO, Map<Long, Long> categorys) {
        List<BigDecimal> dataList = singleSlideMapper.getReferenceScopeCopy(exportAiListVO.getQuantitativeIndicators(), categorys.get(singleId), special.getProjectId(), special.getControlGroup());
        Integer count = singleSlideMapper.getCategoryIdCountByGroupCode(categorys.get(singleId), special.getProjectId(), special.getControlGroup());
        exportAiListVO.setNormalDistribution(MathUtils.getFirstAndLastOfMiddle95Percent(dataList, count));
        if (CollectionUtils.isNotEmpty(dataList)) {
            if (CollectionUtil.isNotEmpty(dataList)) {
                List<BigDecimal> objects = new ArrayList<>(dataList);
                objects.forEach(e -> {
                    if (e.compareTo(BigDecimal.ZERO) < 0) {
                        dataList.remove(e);
                    }
                });
            }
            if (CollectionUtil.isNotEmpty(dataList)) {
                BigDecimal bigDecimal = MathUtils.calculateAve(dataList.toArray(new BigDecimal[dataList.size()]), 3);
                log.info("平均值" + bigDecimal);
                BigDecimal variance = MathUtils.variance(bigDecimal,dataList.toArray(new BigDecimal[dataList.size()]), 3);
                log.info("总体方差" + variance);
                BigDecimal sqrt = MathUtils.sqrt(variance, 3);
                log.info("总体标准差" + sqrt);
                exportAiListVO.setAverageValue(bigDecimal + "±" + sqrt);

            }

        }

    }

}
