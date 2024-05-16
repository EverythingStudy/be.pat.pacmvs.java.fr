package cn.staitech.fr.service.strategy.json;

import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.PathologicalIndicatorCategory;
import cn.staitech.fr.domain.SpecialAnnotationRel;
import cn.staitech.fr.mapper.PathologicalIndicatorCategoryMapper;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: wangfeng
 * @create: 2024-05-16 16:51:08
 * @Description:
 */
@Service
public class CommonParserStrategy {

    @Resource
    private PathologicalIndicatorCategoryMapper pathologicalIndicatorCategoryMapper;

    @Resource
    private SpecialAnnotationRelMapper specialAnnotationRelMapper;

    /**
     * @param jsonTask
     * @param jsonFileS
     */
    public void parseJson(JsonTask jsonTask, JsonFile jsonFileS) {

    }

    /**
     * 查询所有未被删除且登录机构相同的数据
     *
     * @param jsonTask
     * @return
     */
    public Map<String, Long> getPathologicalMap(JsonTask jsonTask) {
        QueryWrapper<PathologicalIndicatorCategory> qw = new QueryWrapper<>();
        // 查询所有未被删除且登录机构相同的数据
        qw.eq("del_flag", 0).eq("organization_id", jsonTask.getOrganizationId());
        List<PathologicalIndicatorCategory> list = pathologicalIndicatorCategoryMapper.selectList(qw);
        Map<String, Long> pathologicalMap = list.stream().collect(
                Collectors.toMap(
                        PathologicalIndicatorCategory::getStructureId,
                        PathologicalIndicatorCategory::getCategoryId,
                        (entity1, entity2) -> entity1));
        return pathologicalMap;
    }

    /**
     * 定位表
     *
     * @param jsonTask
     * @return
     */
    public Long getSequenceNumber(JsonTask jsonTask) {
        LambdaQueryWrapper<SpecialAnnotationRel> SpecialQueryWrapper = new LambdaQueryWrapper<>();
        SpecialQueryWrapper.eq(SpecialAnnotationRel::getSpecialId, jsonTask.getSpecialId());
        SpecialAnnotationRel annotationRel = specialAnnotationRelMapper.selectOne(SpecialQueryWrapper);
        Long sequenceNumber = annotationRel.getSequenceNumber();
        return sequenceNumber;
    }
}
