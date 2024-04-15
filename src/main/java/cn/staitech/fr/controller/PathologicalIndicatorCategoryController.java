package cn.staitech.fr.controller;


import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.PathologicalIndicator;
import cn.staitech.fr.domain.PathologicalIndicatorCategory;
import cn.staitech.fr.service.CategoryService;
import cn.staitech.fr.service.PathologicalIndicatorCategoryService;
import cn.staitech.fr.service.PathologicalIndicatorService;
import cn.staitech.fr.service.SpecialService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Api(value = "脏器标签", tags = "脏器标签")
@RestController
@Validated
@RestControllerAdvice
@RequestMapping("/PathologicalIndicatorCategory")
public class PathologicalIndicatorCategoryController {

    @Resource
    private PathologicalIndicatorService pathologicalIndicatorService;

    @Resource
    private SpecialService specialService;

    @Resource
    private PathologicalIndicatorCategoryService pathologicalIndicatorCategoryService;

    @Resource
    private CategoryService categoryService;


    @ApiOperation(value = "获取GeoJson数据")
    @GetMapping("/getDataList")
    public R<List<PathologicalIndicatorCategory>> getDataList(@RequestParam(value = "specialId") @ApiParam(name = "specialId", value = "专题id", required = true) Long specialId, @RequestParam(value = "categoryId") @ApiParam(name = "categoryId", value = "脏器标签id", required = true) Long categoryId) {
        String organId = categoryService.getById(categoryId).getOrganId();
        Long speciesId = Long.valueOf(specialService.getById(specialId).getSpeciesId());
        QueryWrapper<PathologicalIndicator> pathologicalIndicatorQueryWrapper = new QueryWrapper<>();
        pathologicalIndicatorQueryWrapper.eq("species_id", speciesId).eq("organ_id", organId);
        Long indicatorId = pathologicalIndicatorService.getOne(pathologicalIndicatorQueryWrapper).getIndicatorId();
        QueryWrapper<PathologicalIndicatorCategory> pathologicalIndicatorCategoryQueryWrapper = new QueryWrapper<>();
        pathologicalIndicatorCategoryQueryWrapper.eq("indicator_id", indicatorId);
        return R.ok(pathologicalIndicatorCategoryService.list(pathologicalIndicatorCategoryQueryWrapper));
    }

}
