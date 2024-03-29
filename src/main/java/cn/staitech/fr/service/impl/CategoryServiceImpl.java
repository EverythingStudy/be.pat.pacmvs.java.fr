package cn.staitech.fr.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.staitech.fr.domain.Category;
import cn.staitech.fr.service.CategoryService;
import cn.staitech.fr.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

/**
* @author admin
* @description 针对表【fr_category】的数据库操作Service实现
* @createDate 2024-03-29 10:08:34
*/
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
    implements CategoryService{

}




