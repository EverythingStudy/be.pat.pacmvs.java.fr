package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.SpecialAnnotationRel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author admin
* @description 针对表【fr_special_annotation_rel(项目标注序列关系表)】的数据库操作Mapper
* @createDate 2024-05-10 14:22:08
* @Entity cn.staitech.fr.domain.SpecialAnnotationRel
*/
public interface SpecialAnnotationRelMapper extends BaseMapper<SpecialAnnotationRel> {

    Integer selectTableSpecialCount(Annotation annotation);
    

}




