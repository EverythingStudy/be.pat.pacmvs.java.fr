package cn.staitech.fr.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.staitech.fr.domain.PathologicalIndicatorCategory;
import cn.staitech.fr.mapper.IndicatorMapper;
import cn.staitech.fr.mapper.PathologicalIndicatorCategoryMapper;
import cn.staitech.fr.mapper.StructureMapper;
import cn.staitech.fr.service.PathologicalIndicatorCategoryService;
import cn.staitech.fr.service.StructureService;
import cn.staitech.fr.utils.MessageSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PathologicalIndicatorCategoryServiceImpl implements PathologicalIndicatorCategoryService {
    @Resource
    private PathologicalIndicatorCategoryMapper pathologicalIndicatorCategoryMapper;
    @Resource
    private StructureService structureService;
    @Resource
    private IndicatorMapper indicatorMapper;
    @Resource
    private StructureMapper structureMapper;

    /**
     * 添加标签
     */
    @Override
    public int insertSelective(PathologicalIndicatorCategory pathologicalIndicatorCategory) {
        return pathologicalIndicatorCategoryMapper.insertSelective(pathologicalIndicatorCategory);
    }


    /**
     * 修改标签
     */
    @Override
    public String updateByPrimaryKeySelective(PathologicalIndicatorCategory indicator) {
        if (pathologicalIndicatorCategoryMapper.updateByPrimaryKeySelective(indicator) > 0) {
            return MessageSource.M("UPDATE_SUCCESS_1");
        } else {
            return MessageSource.M("NOT_THIS_INDICATOR");
        }
    }

    /**
     * 删除标签
     */
    @Override
    public String deleteByPrimaryKey(Long categoryId) {
        // 查询标签是否存在
        if (pathologicalIndicatorCategoryMapper.deleteByPrimaryKey(categoryId) > 0) {
            return MessageSource.M("DELETE_SUCCESS");
        } else {
            return MessageSource.M("NOT_THIS_INDICATOR");
        }
    }

    /**
     * 查询标签详细信息
     */
    @Override
    public PathologicalIndicatorCategory selectByPrimaryKey(Long categoryId) {
        return pathologicalIndicatorCategoryMapper.selectByPrimaryKey(categoryId);
    }

    /**
     * 根据病理指标id获取全部信息
     *
     * @param indicatorId 病理指标ID
     * @return 标签信息
     */
    @Override
    public List<PathologicalIndicatorCategory> selectIndicatorIdAll(Long indicatorId) {
        return pathologicalIndicatorCategoryMapper.selectIndicatorIdAll(indicatorId);
    }

    /**
     * 根据病理指标id删除信息
     *
     * @param indicatorId 病理指标ID
     * @return 标签信息
     */
    @Override
    public int delIndicatorCategory(Long indicatorId) {
        return pathologicalIndicatorCategoryMapper.delIndicatorCategory(indicatorId);
    }

    @Override
    public PathologicalIndicatorCategory selectCategoryAll(Long CategoryId) {
        return pathologicalIndicatorCategoryMapper.selectCategoryAll(CategoryId);
    }


    /**
     * 条件查询标注类别
     *
     * @param Pathological 病例指标id 或 颜色 或 标注类别名称
     * @return 结果
     */
    @Override
    public List<PathologicalIndicatorCategory> selectIndicatorMessage(PathologicalIndicatorCategory Pathological) {
        return pathologicalIndicatorCategoryMapper.selectIndicatorMessage(Pathological);
    }


    /**
     * 条件查询标注类别
     *
     * @param Pathological 病例指标id 或 颜色 或 标注类别名称
     * @return 结果
     */
    @Override
    public List<PathologicalIndicatorCategory> selectIndicatorMessageForUpdate(PathologicalIndicatorCategory Pathological) {
        return pathologicalIndicatorCategoryMapper.selectIndicatorMessageForUpdate(Pathological);
    }



    /**
     * 通过项目ID查询标注类别
     *
     * @param projectId 项目ID
     * @return 标注类别列表
     */
    @Override
    public List<PathologicalIndicatorCategory> selectCategoryByProjectId(Long projectId) {
        return pathologicalIndicatorCategoryMapper.selectCategoryByProjectId(projectId);
    }

    /**
     * 查询病例指标下的标注类别数量
     */
    @Override
    public Long selectCategoryNumber(Long indicatorId) {
        return pathologicalIndicatorCategoryMapper.selectCategoryNumber(indicatorId);
    }

    /**
     * 根据projectId查询标注类别
     */
    @Override
    public List<PathologicalIndicatorCategory> selectProjectCategory(Long projectId) {
        return pathologicalIndicatorCategoryMapper.selectProjectCategory(projectId);
    }



    @Override
    public Integer selectLabelNumByStructureId(String structureId) {
        return pathologicalIndicatorCategoryMapper.selectLabelNumByStructureId(structureId);
    }

    
    @Override
    public String updateByPrimaryKeySelective2(PathologicalIndicatorCategory indicator) {
        if (pathologicalIndicatorCategoryMapper.updateByPrimaryKeySelective(indicator) > 0) {
            return "1";
        } else {
            return "0";
        }
    }
}