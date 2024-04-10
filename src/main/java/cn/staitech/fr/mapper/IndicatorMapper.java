package cn.staitech.fr.mapper;

import java.util.List;

import org.springframework.stereotype.Repository;

import cn.staitech.fr.domain.Indicator;

@Repository
public interface IndicatorMapper {

    /**
     * 查询单个指标信息
     *
     * @param indicatorId 指标ID
     * @return 指标信息
     */
    Indicator selectIndicatorById(Long indicatorId);


    /**
     * 查询指标列表
     *
     * @param indicator 指标信息
     * @return 公告集合
     */
    List<Indicator> selectIndicatorList(Indicator indicator);


    /**
     * 新增指标
     *
     * @param indicator 指标信息
     * @return 结果
     */
    int insertIndicator(Indicator indicator);

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
     * 查询指标在项目表中的记录数量
     */
    Integer selectIndicatorCountInProject(Long indicatorId);

    Integer selectIndicatorCountByIndicator(Indicator indicator);


    /**
     * 查询指标列表 不分页
     *
     * @param
     * @return 结果
     */

    List<Indicator> selectIndicatorInformation(Indicator indicator);
}
