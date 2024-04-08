package cn.staitech.fr.controller;


import cn.staitech.fr.utils.ExcludeEmptyQueryWrapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.utils.DateUtils;
import cn.staitech.fr.domain.Category;
import cn.staitech.fr.service.CategoryService;
import cn.staitech.fr.vo.category.CategoryQueryPageIn;
import cn.staitech.system.api.domain.SysUser;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;

import static cn.hutool.core.date.DateUtil.offsetDay;
/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author wanglibei
 * @since 2024-04-02
 */
@Api(value = "脏器标签", tags = "脏器标签")
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    @ApiOperation(value = "脏器标签表列表分页查询")
    @GetMapping("/selectListPage")
    public R<PageResponse<Category>> list(CategoryQueryPageIn req) throws ParseException {
        PageResponse<Category> resp = new PageResponse<>();
        Page<SysUser> page = PageHelper.startPage(req.getPageNum(), req.getPageSize());
        ExcludeEmptyQueryWrapper<Category> categoryQueryWrapper = new ExcludeEmptyQueryWrapper<>();
        categoryQueryWrapper.eq("species",req.getSpecies()).eq("organ_name",req.getOrganName()).like("category_abbreviation",req.getCategoryAbbreviation());
        if(req.getCreateTime() != null){
            Object beginTime = req.getCreateTime().get("beginTime");
            Object endTime = req.getCreateTime().get("endTime");
            if (beginTime != null && !Objects.equals(beginTime, "")) {
                categoryQueryWrapper.ge("create_time", DateUtils.stringToDate((String) beginTime, "yyyy-MM-dd"));
            }
            if (endTime != null && !Objects.equals(endTime, "")) {
                categoryQueryWrapper.lt("create_time", (offsetDay(DateUtils.stringToDate((String) endTime, "yyyy-MM-dd"),1)));
            }
        }
        List<Category> categories = categoryService.list(categoryQueryWrapper);
        resp.setTotal(page.getTotal());
        resp.setList(categories);
        resp.setPages(page.getPages());
        return R.ok(resp);
    }


    @ApiOperation(value = "脏器标签表列表查询")
    @GetMapping("/selectList")
    public R<List<Category>> list() {
        return R.ok(categoryService.list());
    }

}
