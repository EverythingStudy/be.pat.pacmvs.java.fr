package cn.staitech.fr.config;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import cn.staitech.fr.service.CategoryService;

/**
 * 
 * @ClassName: MapConstant
 * @Description:
 * @author wanglibei
 * @date 2024年4月2日
 * @version V1.0
 */
@Component
public class MapConstant {
	/**
	 * 脏器
	 */
	public static Map<Long, String> CATEGORY_MAP;

	
	@Resource
	private CategoryService categoryService;

	/**
	 * 获取脏器名称
	 *
	 * @param getOrgan
	 * @return
	 */
	public static String getCategory(Long categoryId) {
		if (CATEGORY_MAP.containsKey(categoryId)) {
			return CATEGORY_MAP.get(categoryId);
		}
		return "";
	}



	@PostConstruct
	public void init() {
		// 脏器
		CATEGORY_MAP = categoryService.getCategory();

	}
}
