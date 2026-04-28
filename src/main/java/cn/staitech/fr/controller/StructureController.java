package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.R;
import cn.staitech.common.core.web.controller.BaseController;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.domain.Organ;
import cn.staitech.fr.domain.Structure;
import cn.staitech.fr.service.OrganService;
import cn.staitech.fr.service.StructureService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutionException;
import static cn.staitech.fr.constant.Constants.STRUCTURE_RO;

/**
 * @author mugw
 * @version 1.0
 * @description 结构管理
 * @date 2025/5/14 13:44:14
 */
@Api(value = "结构", tags = {"V2.6.0"})
@RestController
@RequestMapping("/structure")
@Slf4j
public class StructureController extends BaseController {
    @Resource
    private StructureService structureService;
    @Resource
    private OrganService organService;

    @ApiOperation(value = "结构列表")
    @GetMapping("/list")
    public R<List<Structure>> list() throws ExecutionException, InterruptedException {
        List<Structure> list = structureService.list(Wrappers.<Structure>lambdaQuery().eq(Structure::getOrganizationId, SecurityUtils.getOrganizationId()));
        return R.ok(list);
    }

    @ApiOperation(value = "结构列表")
    @GetMapping("/getStructureList")
    public R<List<Structure>> getStructureList(@RequestParam(required = true, name = "speciesId") String speciesId,
                                               @RequestParam(required = true, name = "organId") String organId) {
        List<Structure> structures = structureService.list(Wrappers.<Structure>lambdaQuery()
                .eq(Structure::getSpeciesId, speciesId)
                .eq(Structure::getOrganCode, organId)
                .eq(Structure::getType, STRUCTURE_RO)
                .eq(Structure::getOrganizationId, SecurityUtils.getOrganizationId()));
        return R.ok(structures);
    }

    @ApiOperation(value = "脏器列表")
    @GetMapping("/getOrganByspeciesId")
    public R<List<Organ>> getOrganBySpeciesId(@RequestParam(required = true, name = "speciesId") String speciesId) {
        List<Organ> list = organService.list(Wrappers.<Organ>lambdaQuery()
                .eq(Organ::getSpeciesId, speciesId)
                .eq(Organ::getOrganizationId, SecurityUtils.getOrganizationId()));
        return R.ok(list);
    }

}
