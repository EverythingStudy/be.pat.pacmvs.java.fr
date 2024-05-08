package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.R;
import cn.staitech.common.core.utils.uuid.UUID;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.service.AnnotationService;
import cn.staitech.fr.service.SlideService;
import cn.staitech.fr.utils.MessageSource;
import cn.staitech.fr.vo.annotation.AnnotationById;
import cn.staitech.fr.vo.annotation.AnnotationSelectList;
import cn.staitech.fr.vo.annotation.MarkingMerge;
import cn.staitech.fr.vo.geojson.Features;
import cn.staitech.fr.vo.geojson.in.RoiIn;
import cn.staitech.fr.vo.geojson.in.UpdateOperationIn;
import cn.staitech.fr.vo.geojson.in.ViewAddIn;
import cn.staitech.fr.vo.geojson.in.ViewAddInList;
import cn.staitech.fr.vo.geojson.out.BatchResult;
import com.alibaba.fastjson.JSONObject;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/annotation")
public class AnnotationController {

    @Resource
    private AnnotationService annotationService;

    @Resource
    private SlideService slideService;

    @ApiOperation(value = "添加标注")
    @PostMapping("/insert")
    public R<Long> add(@Validated @RequestBody ViewAddIn req) throws Exception {
        req.setTraceId(UUID.fastUUID().toString());
        req.setIsBatch(false);
        Long res = annotationService.insert(req);
        return R.ok(res);
    }

    @ApiOperation(value = "删除标注")
    @ApiImplicitParams({@ApiImplicitParam(name = "annotationId", value = "标注id", required = true, dataType = "Long", paramType = "query")})
    @DeleteMapping("/delete")
    public R<String> del(@Validated @RequestBody AnnotationById req) throws Exception {
        req.setTraceId(UUID.fastUUID().toString());
        req.setIsBatch(false);
        annotationService.delete(req);
        return R.ok(null, MessageSource.M("OPERATE_SUCCEED"));
    }

    @ApiOperation(value = "更新标注")
    @PutMapping("/update")
    public R<String> update(@Validated @RequestBody ViewAddIn req) throws Exception {
        req.setTraceId(UUID.fastUUID().toString());
        req.setIsBatch(false);
        if (req.getUpdate_by() == null) {
            req.setUpdate_by(SecurityUtils.getLoginUser().getSysUser().getUserId());
        }
        annotationService.update(req);
        return R.ok(req.getMarking_id(), MessageSource.M("OPERATE_SUCCEED"));
    }


    @ApiOperation(value = "填充轮廓")
    @PostMapping("/padding")
    public R<String> padding(@Validated @RequestBody AnnotationById req) throws Exception {
        int res = annotationService.padding(req);
        if (res > 0) {
            return R.ok(null, MessageSource.M("OPERATE_SUCCEED"));
        } else {
            return R.fail(null, MessageSource.M("OPERATE_ERROR"));
        }
    }

    @ApiOperation(value = "复制/粘贴轮廓")
    @PostMapping("/stickup")
    public R<String> stickup(@Validated @RequestBody AnnotationById req) throws Exception {
        int res = annotationService.stickup(req);
        if (res > 0) {
            return R.ok(null, MessageSource.M("OPERATE_SUCCEED"));
        } else {
            return R.fail(null, MessageSource.M("OPERATE_ERROR"));
        }
    }

    @ApiOperationSupport(author = "zmj")
    @ApiOperation(value = "添加ROI轮廓")
    @PostMapping("/intelligentAnno/insertROI")
    public R<String> addList(@Validated @RequestBody RoiIn req) throws Exception {
        if (CollectionUtils.isEmpty(req.getGeometryList())) {
            return R.fail(MessageSource.M("NO_DATA_TRANSFERRED"));
        }
        if (!req.getRoiStatus().equals(1) && !req.getRoiStatus().equals(0)) {
            return R.fail(MessageSource.M("ARGUMENT_INVALID"));
        }
        return annotationService.roiContDel(req);
    }

    @ApiOperationSupport(author = "gjt")
    @ApiOperation(value = "合并轮廓预览")
    @PostMapping("/markingMerge")
    public R<JSONObject> markingMerge(@Validated @RequestBody MarkingMerge req) throws Exception {
        JSONObject res = annotationService.markingMerge(req);
        return R.ok(res);
    }

    @ApiOperation(value = "获取GeoJson数据")
    @PostMapping("/selectLists")
    public R<List<Features>> selectLists(@Validated @RequestBody AnnotationSelectList req) throws Exception {
        return R.ok(annotationService.selectListBy(req));
    }

    /**
     * TODO:
     * 2
     *
     * @param req
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "合并、裁剪轮廓")
    @PutMapping("/updateOperation")
    public R<JSONObject> updateOperation(@Validated @RequestBody UpdateOperationIn req) throws Exception {
        if (req.getUpdate_by() == null) {
            req.setUpdate_by(SecurityUtils.getLoginUser().getSysUser().getUserId());
        }
        JSONObject geoJson = annotationService.updateOperation(req, UUID.fastUUID().toString(), false);
        return R.ok(geoJson, MessageSource.M("OPERATE_SUCCEED"));
    }

    @ApiOperation(value = "批量操作")
    @PostMapping("/batch")
    public R<List<BatchResult>> batch(@Validated @RequestBody ViewAddInList list) throws Exception {
        if (CollectionUtils.isEmpty(list.getList())) {
            return R.fail(MessageSource.M("ARGUMENT_INVALID"));
        }
        List<BatchResult> result = annotationService.batch(list.getList());
        return R.ok(result, MessageSource.M("OPERATE_SUCCEED"));
    }


}

