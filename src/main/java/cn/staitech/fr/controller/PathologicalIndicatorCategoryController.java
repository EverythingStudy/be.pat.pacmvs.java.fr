package cn.staitech.fr.controller;


import cn.staitech.common.core.domain.R;
import cn.staitech.common.security.utils.SecurityUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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


    @ApiOperation(value = "获取单脏器下所有标签")
    @GetMapping("/selectCategoryList")
    public R<List<PathologicalIndicatorCategory>> getDataList(@RequestParam(value = "specialId") @ApiParam(name = "specialId", value = "专题id", required = true) Long specialId, @RequestParam(value = "categoryId") @ApiParam(name = "categoryId", value = "脏器标签id", required = true) Long categoryId) {
        String organId = categoryService.getById(categoryId).getOrganId();
        if(organId == null){
            return R.ok(new ArrayList<>());
        }
        List<String> organList = new ArrayList<>();
//        switch (organId) {
//            case "7D":
//                organList.add("3E");
//                organList.add("3D");
//                break;
//            case "7C":
//                organList.add("24");
//                organList.add("25");
//                break;
//            case "7B":
//                organList.add("5F");
//                organList.add("3F");
//                break;
//            case "7A":
//                organList.add("21");
//                organList.add("23");
//                break;
//            case "53":
//                organList.add("50");
//                organList.add("51");
//                break;
//            default:
//                organList.add(organId);
//                break;
//        }
        organList.add(organId);
        Long speciesId = Long.valueOf(specialService.getById(specialId).getSpeciesId());
        QueryWrapper<PathologicalIndicator> pathologicalIndicatorQueryWrapper = new QueryWrapper<>();
        pathologicalIndicatorQueryWrapper
                .eq("del_flag", 0)
                .eq("species_id", speciesId)
                .eq("organization_id", SecurityUtils.getLoginUser().getSysUser().getOrganizationId())
                .in("organ_id", organList);
        List<PathologicalIndicator> indicator = pathologicalIndicatorService.list(pathologicalIndicatorQueryWrapper);
        if (indicator == null) {
            return R.ok(new ArrayList<>());
        }
        List<Long> indicatorIds = indicator.stream().map(PathologicalIndicator::getIndicatorId).collect(Collectors.toList());
        if(indicatorIds.size() == 0){
            return R.ok(new ArrayList<>());
        }
        QueryWrapper<PathologicalIndicatorCategory> pathologicalIndicatorCategoryQueryWrapper = new QueryWrapper<>();
        pathologicalIndicatorCategoryQueryWrapper.in("indicator_id", indicatorIds);
        return R.ok(pathologicalIndicatorCategoryService.list(pathologicalIndicatorCategoryQueryWrapper));
    }

}
