package cn.staitech.fr.service;
import java.util.List;

import cn.staitech.fr.domain.Indicator;

public interface IndicatorService {

    /**
     * 添加病例指标
     *
     * @param indicator 添加的字段信息
     * @return 结果
     */
    int insertIndicator(Indicator indicator);



    /**
     * 展示病例指标详情
     *
     * @param indicatorId 病例指标id
     * @return 结果
     */
    Indicator selectIndicatorsById(Long indicatorId);


    /**
     * 删除
     *
     * @param indicatorId 指标id
     * @return 结果
     */
    int delIndicator(Long indicatorId);



    /**
     * 查询指标列表
     *
     * @param indicator
     * @return 结果
     */
    List<Indicator> selectIndicator(Indicator indicator);



    /**
     * 查询所有的病理数量
     */
    Integer selectIndicatorNum();


    // 2.0 新修改====================================

    /**
     * 查询专题在项目表中的数量
     */
    Integer selectIndicatorCountInProject(Long indicatorId);


    Integer selectIndicatorCountByIndicator(Indicator indicator);

    /**
     * 查询指标列表
     *
     * @param
     * @return 结果
     */
    List<Indicator> selectIndicatorInformation(Indicator indicator);


}
