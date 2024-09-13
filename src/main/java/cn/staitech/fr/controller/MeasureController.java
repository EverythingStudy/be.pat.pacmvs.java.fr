package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.log.annotation.Log;
import cn.staitech.common.log.enums.BusinessType;
import cn.staitech.fr.service.MeasureService;
import cn.staitech.fr.utils.MessageSource;
import cn.staitech.fr.vo.annotation.Features;
import cn.staitech.fr.vo.annotation.in.ViewAddIn;
import cn.staitech.fr.vo.measure.MarkingSelectListVO;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Api(value = "viewer页面-测量", tags = "viewer页面-测量")
@Slf4j
@RestController
@RequestMapping("/measure")
public class MeasureController {


    @Resource
    private MeasureService measureService;


    @ApiOperation(value = "获取测量列表")
    @GetMapping("/list")
    public R<PageResponse<MarkingSelectListVO>> list(
            @NotNull(message = "{ReviewRoundController.list.isnull}") @RequestParam("pageNum") @ApiParam(name = "pageNum", value = "分页参数", required = true) Integer pageNum,
            @NotNull(message = "{ReviewRoundController.list.isnull}") @RequestParam("pageSize") @ApiParam(name = "pageSize", value = "分页参数", required = true) Integer pageSize,
            @RequestParam(value = "measureFullName", required = false) @ApiParam(name = "measureFullName", value = "标注名称", required = true) String measureFullName,
            @RequestParam(value = "slideId") @ApiParam(name = "slideId", value = "切片ID", required = true) Long slideId) throws Exception {
        if (!Optional.ofNullable(slideId).isPresent()) {
            return R.fail(MessageSource.M("ARGUMENT_INVALID"));
        }

        return R.ok(measureService.list(slideId, pageNum, pageSize, measureFullName));
    }


    @ApiOperation(value = "获取GeoJson数据")
    @GetMapping("/getDataList")
    public R<List<Features>> getDataList(@RequestParam(value = "slideId") @ApiParam(name = "slideId", value = "切片ID", required = true) Long slideId) throws Exception {
        return R.ok(measureService.selectListBy(slideId));
    }


    @ApiOperation(value = "添加测量")
    @PostMapping("/add")
    public R<Long> add(@Validated @RequestBody ViewAddIn req) throws Exception {
        Long markingId = measureService.insert(req);
        return R.ok(markingId, MessageSource.M("OPERATE_SUCCEED"));
    }


    @ApiOperation(value = "删除测量")
    @ApiImplicitParams({@ApiImplicitParam(name = "markingId", value = "标注id", required = true, dataType = "Long", paramType = "query")})
    @DeleteMapping("/del")
    public R<String> del(@RequestParam(value = "marking_id") @ApiParam(name = "marking_id", value = "标注id", required = true) Long marking_id) throws Exception {
        measureService.delete(marking_id);
        return R.ok(null, MessageSource.M("OPERATE_SUCCEED"));
    }


    @Log(title = "标注测量excel导出", businessType = BusinessType.EXPORT)
    @ApiOperation(value = "标注测量excel导出")
    @GetMapping("/export")
    public void export(@RequestParam(value = "singleSlideId") @ApiParam(name = "singleSlideId", value = "切片ID", required = true) Long singleSlideId, HttpServletResponse response) throws Exception {
        measureService.execlExport(singleSlideId, response);
    }

}
