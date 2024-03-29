package cn.staitech.fr.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import cn.staitech.fr.domain.PathologicalIndicatorCategory;

public interface PathologicalIndicatorCategoryMapper extends BaseMapper<PathologicalIndicatorCategory> {

    /**
     * 根据code查询出当前一组数据的roe标签
     *
     * @param map
     * @return
     */
    PathologicalIndicatorCategory selectRoe(Map<String, Object> map);

    /**
     * 根据主键删除信息
     *
     * @param categoryId
     * @return
     */
    int deleteByPrimaryKey(Long categoryId);

    /**
     * 添加标签信息
     *
     * @param record
     * @return
     */
    int insertSelective(PathologicalIndicatorCategory record);

    /**
     * 根据主键查询单挑标签信息
     *
     * @param categoryId
     * @return
     */
    PathologicalIndicatorCategory selectByPrimaryKey(Long categoryId);

    /**
     * 修改标签信息
     *
     * @param record
     * @return
     */
    int updateByPrimaryKeySelective(PathologicalIndicatorCategory record);

    /**
     * 根据病理指标id获取全部信息
     *
     * @param indicatorId
     * @return
     */
    List<PathologicalIndicatorCategory> selectIndicatorIdAll(Long indicatorId);


    /**
     * 根据病理指标id删除标注类别
     *
     * @param indicatorId
     * @return
     */
    int delIndicatorCategory(Long indicatorId);

    @SuppressWarnings("checkstyle:ParameterName")
    PathologicalIndicatorCategory selectCategoryAll(Long CategoryId);

    /**
     * 条件查询标注类别
     *
     * @param Pathological 病例指标id 或 颜色 或 标注类别名称
     * @return 结果
     */
    List<PathologicalIndicatorCategory> selectIndicatorMessage(PathologicalIndicatorCategory Pathological);


    /**
     * 条件查询标注类别-用于修改，排除自己
     *
     * @param Pathological 病例指标id 或 颜色 或 标注类别名称
     * @return 结果
     */
    List<PathologicalIndicatorCategory> selectIndicatorMessageForUpdate(PathologicalIndicatorCategory Pathological);

    /**
     * 通过项目ID查询标注类别
     *
     * @param projectId 项目ID
     * @return 标注类别列表
     */
    List<PathologicalIndicatorCategory> selectCategoryByProjectId(Long projectId);

    /**
     * 查询病例指标下的标注类别数量
     */
    Long selectCategoryNumber(Long indicatorId);

    /**
     * 根据projectId查询标注类别
     */
    List<PathologicalIndicatorCategory> selectProjectCategory(Long projectId);

    /**
     * 查询标签所属脏器系统内已有标签数量
     */

    Integer selectLabelNumByStructureId(String structureId);

    /**
     * 根据项目和结构编码查询详情数据
     */
    PathologicalIndicatorCategory selectProjectAndNumber(@Param("projectId") Long projectId, @Param("number") String number, @Param("organizationId") Long organizationId);

    String selectCategoryById(@Param("split") String[] split);
}