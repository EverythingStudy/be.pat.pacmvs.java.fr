package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.PathologicalIndicatorCategory;
import cn.staitech.fr.vo.category.CategorySize;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author admin
* @description 针对表【tb_pathological_indicator_category(tb_pathological_indicator_category)】的数据库操作Mapper
* @createDate 2024-04-10 16:36:25
* @Entity cn.staitech.fr.domain.PathologicalIndicatorCategory
*/
public interface PathologicalIndicatorCategoryMapper extends BaseMapper<PathologicalIndicatorCategory> {
	List<CategorySize> selectCategorySize(List<Long> indicatorIds);
}




