package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.R;
import cn.staitech.common.log.annotation.Log;
import cn.staitech.common.log.enums.BusinessType;
import cn.staitech.fr.domain.Outline;
import cn.staitech.fr.service.OutlineService;
import cn.staitech.fr.utils.MessageSource;
import cn.staitech.fr.vo.outline.OutlineSelectVO;
import cn.staitech.fr.vo.outline.OutlineStatistic;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@Slf4j
@Api(value = "Viewer-吸管", tags = "Viewer-吸管")
@RestController
@Validated
@RestControllerAdvice
@RequestMapping("/outline")
public class OutlineRedisController {

    @Resource(name = "OutlineRedisServiceImpl")
    private OutlineService outlineService;

    /**
     * Viewer-吸管-取消（删除所有当前用户的记录）
     * eg: /outline/clean?createBy=1
     *
     * @param createBy 创建者ID
     * @return true：成功，false：失败
     */
    @ApiOperation(value = "Viewer-吸管-取消")
    @Log(title = "Viewer-吸管-取消", menu = "Viewer-吸管-取消", subMenu = "Viewer-吸管-取消", businessType = BusinessType.CLEAN)
    @PostMapping("/clean")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "createBy", value = "创建者ID", required = true, dataType = "Long", paramType = "query")})
    public R clean(@RequestParam @ApiParam(name = "createBy", value = "创建者ID", required = true) Long createBy) {
        outlineService.removeByCreateByAndToken(createBy, null);
        return R.ok(MessageSource.M("OPERATE_SUCCEED"));
    }

    /**
     * Viewer-吸管-数据表
     *
     * @param selectVO
     * @return 所有数据
     */
    @ApiOperation(value = "Viewer-吸管-数据表")
    @Log(title = "Viewer-吸管-数据表", menu = "Viewer-吸管-数据表", subMenu = "Viewer-吸管-数据表", businessType = BusinessType.QUERY)
    @PostMapping("/list")
    public R<OutlineStatistic> list(@Validated @RequestBody OutlineSelectVO selectVO) {
        // 删除当前用户、非当前slideId的记录
        outlineService.removeBycreateBySlideId(selectVO.getCreateBy(), selectVO.getSlideId());

        if (selectVO.getMinVal() != null && selectVO.getMaxVal() != null && (selectVO.getMinVal() > selectVO.getMaxVal())) {
            return R.fail(MessageSource.M("OUTLINE.ARGUEMENT.ERROR"));
        }

        List<Outline> list = outlineService.selectList(selectVO);

        if (CollectionUtils.isEmpty(list)) {
            return R.fail(MessageSource.M("OUTLINE.NORESULT"));
        }

        return R.ok(outlineService.statistic(list, selectVO.getBizType()));
    }

    /**
     * Viewer-吸管-保存为标注
     *
     * @param selectVO
     * @return
     */
    @ApiOperation(value = "Viewer-吸管-保存为标注")
    @Log(title = "Viewer-吸管-保存为标注", menu = "Viewer-吸管-保存为标注", subMenu = "Viewer-吸管-保存为标注", businessType = BusinessType.INSERT)
    @PostMapping("/save")
    public R save(@Validated @RequestBody OutlineSelectVO selectVO) throws Exception {
        if (selectVO.getMinVal() != null && selectVO.getMaxVal() != null && (selectVO.getMinVal() > selectVO.getMaxVal())) {
            return R.fail(MessageSource.M("OUTLINE.ARGUEMENT.ERROR"));
        }
        List<Outline> list = outlineService.selectList(selectVO);
        if (CollectionUtils.isEmpty(list)) {
            return R.fail(MessageSource.M("OUTLINE.NORESULT"));
        }
        outlineService.saveAll(list, selectVO);
        return R.ok(MessageSource.M("OPERATE_SUCCEED"));
    }
}
