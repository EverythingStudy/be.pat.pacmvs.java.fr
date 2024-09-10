package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.Annotation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author admin
* @description 针对表【fr_contour】的数据库操作Mapper
* @createDate 2024-09-10 09:31:06
* @Entity cn.staitech.fr.domain.Contour
*/
public interface AnnotationMapper extends BaseMapper<Annotation> {
	Integer selectExistTable(Annotation annotation);

	void createTableSequence(Annotation annotation);

	void createTable(Annotation annotation);

    Integer selectTableRecordCount(Annotation annotation);

	Annotation stClosestPoint(Annotation annotation);

	Annotation stDistance(Annotation annotation);

	Annotation avgDistance(Annotation annotation);

	Annotation selectByIds(Annotation annotation);



}




