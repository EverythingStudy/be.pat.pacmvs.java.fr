package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.vo.annotation.in.MarkingMerge;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author admin
* @description 针对表【fr_contour】的数据库操作Mapper
* @createDate 2024-09-10 09:31:06
* @Entity cn.staitech.fr.domain.Contour
*/
@DS("slave")
public interface AnnotationMapper extends BaseMapper<Annotation> {

	List<Annotation> selectListBy(Annotation annotation);

	Integer selectExistTable(Annotation annotation);

	void createTableSequence(Annotation annotation);

	void createTable(Annotation annotation);

    Integer selectTableRecordCount(Annotation annotation);

	Annotation stClosestPoint(Annotation annotation);

	Annotation stDistance(Annotation annotation);

	Annotation avgDistance(Annotation annotation);

	Annotation selectByIds(Annotation annotation);

	List<Annotation> selectInList(MarkingMerge req);

	Annotation mergeContour(Annotation annotation);

	Annotation selectContourType(Annotation annotation);

	Annotation getArea(Annotation annotation);

	int insert(Annotation annotation);

	int deleteByIds(Annotation annotation);


	int updateByIds(Annotation annotation);

	Integer getCountByCategory(Annotation annotation);

}




