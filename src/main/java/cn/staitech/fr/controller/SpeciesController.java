package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.Species;
import cn.staitech.fr.service.SpeciesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 
* @ClassName: SpeciesController
* @Description:种属处理
* @author wanglibei
* @date 2024年9月10日
* @version V1.0
 */
@Api(tags = "种属")
@RestController
@RequestMapping("/species")
public class SpeciesController {

    @Autowired
    private SpeciesService speciesService;

    @ApiOperation(value = "种属下拉框", notes = "种属下拉框")
    @GetMapping("/speciesList")
    public R<List<Species>> speciesList() {
        List<Species> speciesList = speciesService.getSpeciesList();
        return R.ok(speciesList);
    }

}
