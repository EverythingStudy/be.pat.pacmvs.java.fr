package cn.staitech.fr.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.staitech.common.core.domain.CustomPage;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.domain.StructureTag;
import cn.staitech.fr.domain.StructureTagSet;
import cn.staitech.fr.vo.structure.*;
import cn.staitech.fr.mapper.StructureTagMapper;
import cn.staitech.fr.mapper.StructureTagSetMapper;
import cn.staitech.fr.service.StructureTagService;
import cn.staitech.fr.service.StructureTagSetService;
import cn.staitech.fr.utils.MessageSource;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

import static cn.staitech.common.security.utils.SecurityUtils.isAdmin;


/**
 * @author mugw
 * @version 1.0
 * @description 结构标签管理
 * @date 2025/5/14 13:44:14
 */
@Api(value = "结构标签管理", tags = {"V2.6.0"})
@RestController
@RequestMapping("/structureTag")
public class StructureTagController {

    @Resource
    private StructureTagSetService structureTagSetService;
    @Resource
    private StructureTagSetMapper structureTagSetMapper;
    @Resource
    private StructureTagService structureTagService;
    @Resource
    private StructureTagMapper structureTagMapper;

    @ApiOperation(value = "结构标签新增")
    @PostMapping("/add")
    public R<StructureTag> addTag(@Validated @RequestBody StructureTagVo req)  throws Exception{
        return structureTagService.addTag(req);
    }

    @ApiOperation(value = "结构标签修改")
    @PostMapping("/edit")
    public R<StructureTag> editTag(@RequestBody StructureTagVo req) throws Exception {
        return structureTagService.editTag(req);
    }

    @ApiOperation(value = "查询结构标签列表")
    @PostMapping("/queryTag")
    public R<List<StructureTagPageVo>> queryTag(@RequestBody StructureTagPageReq req) throws Exception {
        return R.ok(structureTagMapper.queryTag(req));
    }

    @ApiOperation(value = "获取标注类别列表接口")
    @PostMapping("/all")
    public R<CustomPage<StructureTagPageVo>> query(@RequestBody StructureTagPageReq req) throws Exception {
        return structureTagService.pageTag(req);
    }

    /**
     * 配置标签-删除 .
     */
    @ApiOperation(value = "标签删除接口")
    @PostMapping("/del/{structureTagId}")
    public R delTag(@PathVariable @NotNull Long structureTagId) throws Exception {
        return structureTagService.delTag(structureTagId);
    }


    @ApiOperation(value = "获取标签详情接口", notes = "wangfeng")
    @GetMapping(value = "/details")
    public R<StructureTagPageVo> getTag(@RequestParam @ApiParam(name = "categoryId", value = "标注类别id", required = true) Long categoryId) {
        StructureTagPageReq req = new StructureTagPageReq();
        req.setStructureTagIds(Arrays.asList(categoryId));
        List<StructureTagPageVo> list = structureTagMapper.queryTag(req);
        return R.ok(CollectionUtil.isEmpty(list) ? null : list.get(0));
    }

    @ApiOperation(value = "结构标签集管理列表分页查询", tags = {"V2.6.0"})
    @PostMapping("/pageTagSet")
    public R<CustomPage<StructureTagSetVo>> pageTagSet(@RequestBody StructureTagSetPageReq req) throws Exception {
        if (!isAdmin(SecurityUtils.getUserId())) {
            req.setOrganizationId(SecurityUtils.getOrganizationId());
        }
        CustomPage<StructureTagSetVo> page = new CustomPage<>(req);
        structureTagSetMapper.pageStructureTagSet(page, req);
        return R.ok(page);
    }

    @ApiOperation(value = "结构标签集管理列表查询", tags = {"V2.6.0"})
    @PostMapping("/queryTagSet")
    public R<List<StructureTagSetVo>> queryTagSet(@RequestBody StructureTagSetPageReq req) throws Exception {
        if (!isAdmin(SecurityUtils.getUserId())) {
            req.setOrganizationId(SecurityUtils.getOrganizationId());
        }
        return R.ok(structureTagSetMapper.queryStructureTagSet(req));
    }


    @ApiOperation(value = "新增结构标签集", tags = {"V2.6.0"})
    @PostMapping("/addTagSet")
    public R<StructureTagSet> addTagSet(@RequestBody StructureTagSetVo req) throws Exception {
        return structureTagSetService.addTagSet(req);
    }

    @ApiOperation(value = "修改结构标签集", tags = {"V2.6.0"})
    @PostMapping("/updateTagSet")
    public R<StructureTagSet> updateTagSet(@RequestBody StructureTagSetVo req) throws Exception {
        return structureTagSetService.updateTagSet(req);
    }


    @ApiOperation(value = "删除结构标签集", tags = {"V2.6.0"})
    @PostMapping("/delSet/{tagSetId}")
    public R<String> delTagSet(@PathVariable @NotNull Long tagSetId) throws Exception {
        return structureTagSetService.delTagSet(tagSetId);
    }

    @ApiOperation(value = "获取结构标签集", tags = {"V2.6.0"})
    @GetMapping(value = "/{structureTagSetId}")
    public R<StructureTagSet> getTagSet(@PathVariable @NotNull Long structureTagSetId) {
        return R.ok(structureTagSetService.getById(structureTagSetId));
    }

    @ApiOperation(value = "检查标签集是否已绑定项目")
    @GetMapping("/check")
    public R<Integer> checkBoundProject(@RequestParam @ApiParam(name = "indicatorId", value = "病理指标id", required = true) Long indicatorId) {
        StructureTagSet structureTagSet = structureTagSetService.getById(indicatorId);
        if (structureTagSet != null && structureTagSetService.isBoundProject(structureTagSet.getSpeciesId())) {
            return R.fail(MessageSource.M("ALREADY_BOUND"));
        }else{
            return R.ok(1);
        }
    }
}
