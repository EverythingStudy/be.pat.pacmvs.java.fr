package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.vo.annotation.AnnotationCountByCategory;
import cn.staitech.fr.vo.annotation.MarkingMerge;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author admin
 * @description 针对表【fr_annotation】的数据库操作Mapper
 * @createDate 2024-04-01 09:42:42
 * @Entity cn.staitech.fr.domain.Annotation
 */
@Mapper
@DS("slave")
public interface AnnotationMapper extends BaseMapper<Annotation> {

    int insert(Annotation annotation);

    List<Annotation> selectListBy(Annotation annotation);

    List<Annotation> aiSelectListBy(Annotation annotation);

    Annotation mergeContour(Annotation annotation);

    Annotation selectContourType(Annotation annotation);

    Integer selectTableRecordCount(Annotation annotation);

    Integer selectExistTable(Annotation annotation);

    void createTableSequence(Annotation annotation);

    Annotation getArea(Annotation annotation);

    Annotation getOrganArea(Annotation annotation);

    Annotation getStructureArea(Annotation annotation);

    void createTable(Annotation annotation);

    void batchSave(Annotation annotation);

    @DS("slave")
    Annotation selectById(Annotation annotation);

    int deleteById(Annotation annotation);

    List<Annotation> selectInList(MarkingMerge req);

    int updateById(Annotation annotation);

    List<Annotation> selectCategoryList(Annotation annotation);

    List<AnnotationCountByCategory> getCategoryCount(Long slideId);

    List<Annotation> getAnnoListByParm(List<Long> slideIdList);

    Annotation collectGeometry(Long singleSlideId);

    Annotation intersectsGeometry(Annotation annotation);

    Integer countDucts(Annotation annotation1);

    Annotation stIsValid(Annotation annotation);

    Integer deleteAiAnnotation(Annotation annotation);
}




