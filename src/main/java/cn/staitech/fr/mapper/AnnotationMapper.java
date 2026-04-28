package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.Annotation;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

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

    List<Annotation> aiSelectList(Annotation annotation);

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

    Annotation selectByIds(Long annotationId);

    Annotation aiSelectById(Annotation annotation);

    int deleteById(Annotation annotation);

    //List<Annotation> selectInList(MarkingMerge req);

    int updateById(Annotation annotation);

    List<Annotation> selectCategoryList(Annotation annotation);

    //List<AnnotationCountByCategory> getCategoryCount(Annotation annotation);

    List<Annotation> getAnnoListByParm(Annotation annotation);

    Annotation collectGeometry(Long singleSlideId);

    Annotation unionGeometryArea(Long singleSlideId);

    Annotation stUnionContourArea(Annotation annotation);

    Annotation intersectsGeometry(Annotation annotation);

    Integer countDucts(Annotation annotation1);

    Annotation stIsValid(Annotation annotation);

    Annotation stIsValidAnnotation(Annotation annotation);

    List<Annotation> getDelAnnotation(Annotation annotation);

    Integer deleteAiAnnotation(Annotation annotation);

    Annotation getInsideOrOutside(Annotation annotation);

    List<Annotation> getInsideOrOutsideList(Annotation annotation);

    Annotation getInsideOrOutsideCount(Annotation annotation);

    Annotation collectAiGeometry(Annotation annotation);

    int batchDeleteBySsIds(Map<String, Object> parm);

    Annotation stEnvelope(Annotation annotation);

    Annotation stContains(Annotation annotation);

    List<Annotation> selectAnnotationIsValid(Annotation annotation);

    Annotation stClosestPoint(Annotation annotation);

    Annotation stDistance(Annotation annotation);

    Annotation avgDistance(Annotation annotation);

    int aiUpdateById(Annotation annotation);

    List<Annotation> getSpinalCordAnno(Annotation annotation);

    Annotation stMakeValid(Annotation annotation);

    void dropTable(Annotation annotation);

    void transferData(Map params);

    List<Annotation> selectIdList(Annotation annotation);

    Annotation collectGeometryStIsValid(Long singleSlideId);

    List<Annotation> getAIDataList(Annotation annotation);

    Annotation getCollectGeometryStIsValid(Annotation annotation);

    Annotation getCollectGeometryIsValid(Annotation annotation);

    Integer deleteBySingleSlideIdBatch(@Param("annotation") Annotation annotation, @Param("batchSize")int batchSize);

    void batchUpdate(Annotation annotation);
}




