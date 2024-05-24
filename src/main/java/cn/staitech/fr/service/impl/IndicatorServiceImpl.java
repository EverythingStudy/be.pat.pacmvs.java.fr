package cn.staitech.fr.service.impl;

import cn.staitech.fr.domain.Indicator;
import cn.staitech.fr.mapper.IndicatorMapper;
import cn.staitech.fr.service.IndicatorService;
import cn.staitech.fr.utils.LanguageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author wangf
 */
@Slf4j
@Service
public class IndicatorServiceImpl implements IndicatorService {
    @Resource
    private IndicatorMapper indicatorMapper;

    /**
     * 添加病例指标
     *
     * @param indicator 添加的字段信息
     * @return 结果
     */
    @Override
    public int insertIndicator(Indicator indicator) {
        return indicatorMapper.insertIndicator(indicator);
    }


    /**
     * 展示病例指标详情
     *
     * @param indicatorId 病例指标id
     * @return 结果
     */
    @Override
    public Indicator selectIndicatorsById(Long indicatorId) {
        return indicatorMapper.selectIndicatorById(indicatorId);
    }


    /**
     * 删除
     *
     * @param indicatorId 指标id
     * @return 结果
     */
    @Override
    public int delIndicator(Long indicatorId) {
        return indicatorMapper.delIndicator(indicatorId);
    }


    /**
     * 查询指标列表
     *
     * @param indicator
     * @return 结果
     */
    @Override
    public List<Indicator> selectIndicator(Indicator indicator) {
        return indicatorMapper.selectIndicator(indicator);
    }


    /**
     * 查询所有的病理数量
     */
    @Override
    public Integer selectIndicatorNum() {
        return indicatorMapper.selectIndicatorNum();
    }

    /**
     * 查询指标在项目表中的记录数量
     */
    @Override
    public Integer selectIndicatorCountInProject(Long indicatorId) {
        return indicatorMapper.selectIndicatorCountInProject(indicatorId);
    }

    /**
     * 查询指标列表
     *
     * @param
     * @return 结果
     */
    @Override
    public List<Indicator> selectIndicatorInformation(Indicator indicator) {
        List<Indicator> list = indicatorMapper.selectIndicatorInformation(indicator);
        for (Indicator obj : list) {
            if (LanguageUtils.isEn()) {
                obj.setIndicatorName(obj.getIndicatorNameEn());
            }
        }
        return list;
    }


    @Override
    public Integer selectIndicatorCountByIndicator(Indicator indicator) {
        return indicatorMapper.selectIndicatorCountByIndicator(indicator);
    }
}
