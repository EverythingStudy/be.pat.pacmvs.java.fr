package cn.staitech.fr.service;

import cn.staitech.fr.domain.Category;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author admin
* @description 针对表【fr_category】的数据库操作Service
* @createDate 2024-03-29 10:08:34
*/
public interface CategoryService extends IService<Category> {
	 Map<String, String> getCategory();
}
