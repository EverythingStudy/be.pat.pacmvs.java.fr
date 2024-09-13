package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.AnnotationDel;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author admin
* @description 针对表【fr_annotation_del】的数据库操作Mapper
* @createDate 2024-09-12 13:38:05
* @Entity cn.staitech.fr.domain.AnnotationDel
*/
@DS("slave")
public interface AnnotationDelMapper extends BaseMapper<AnnotationDel> {


    int insert(AnnotationDel annotationDel);

}




