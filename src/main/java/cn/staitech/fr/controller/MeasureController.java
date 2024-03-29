package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.log.annotation.Log;
import cn.staitech.common.log.enums.BusinessType;
import cn.staitech.fr.service.MeasureService;
import cn.staitech.fr.utils.MessageSource;
import cn.staitech.fr.vo.geojson.Features;
import cn.staitech.fr.vo.geojson.in.MarkingUpdateIn;
import cn.staitech.fr.vo.geojson.in.UpdateOperationIn;
import cn.staitech.fr.vo.geojson.in.ViewAddIn;
import cn.staitech.fr.vo.measure.MeasureSelectPageVo;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Data
public class MeasureController {


    @Resource
    private MeasureService measureService;


    @ApiOperation(value = "获取测量列表")
    @GetMapping("/list")
    public R<PageResponse<MeasureSelectPageVo>> list(
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
        if (!Optional.ofNullable(slideId).isPresent()) {
            return R.fail(MessageSource.M("ARGUMENT_INVALID"));
        }
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


    @ApiOperation(value = "更新测量")
    @PutMapping("/update")
    public R<Long> update(@Validated @RequestBody MarkingUpdateIn req) throws Exception {
        measureService.update(req);
        return R.ok(req.getMarking_id(), MessageSource.M("OPERATE_SUCCEED"));
    }


    @ApiOperation(value = "合并、裁剪轮廓")
    @PutMapping("/updateOperation")
    public R<JSONObject> updateOperation(@Validated @RequestBody UpdateOperationIn req) throws Exception {
        JSONObject geoJson = measureService.updateOperation(req);
        return R.ok(geoJson, MessageSource.M("OPERATE_SUCCEED"));
    }


    @ApiOperation(value = "合并、裁剪轮廓校验")
    @PutMapping("/operationCheck")
    public R<Double> operationCheck(@Validated @RequestBody UpdateOperationIn req) throws Exception {
        double percentage = measureService.operationCheck(req);
        return R.ok(percentage, MessageSource.M("OPERATE_SUCCEED"));
    }


    @Log(title = "标注测量excel导出", businessType = BusinessType.EXPORT)
    @ApiOperation(value = "标注测量excel导出")
    @GetMapping("/export")
    public void export(@RequestParam(value = "slideId") @ApiParam(name = "slideId", value = "切片ID", required = true) Long slideId, HttpServletResponse response) throws Exception {
        measureService.execlExport(slideId, response);
    }





























}
