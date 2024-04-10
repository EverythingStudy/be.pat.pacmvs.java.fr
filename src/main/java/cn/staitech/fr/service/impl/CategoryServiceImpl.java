package cn.staitech.fr.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.staitech.fr.domain.Category;
import cn.staitech.fr.mapper.CategoryMapper;
import cn.staitech.fr.service.CategoryService;

/**
* @author admin
* @description 针对表【fr_category】的数据库操作Service实现
* @createDate 2024-03-29 10:08:34
*/
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService{
	
	@Resource
    private CategoryMapper categoryMapper;

	@Override
	public Map<String, String> getCategory() {
		Map<String, String> map = new HashMap<String, String>();
		List<Category> list = list();
		if (CollectionUtils.isNotEmpty(list)) {
			map = list.stream().collect(Collectors.toMap(item -> item.getOrganizationId().toString() + item.getCategoryId(), Category::getOrganName));
		}
		return map;
	}


}




