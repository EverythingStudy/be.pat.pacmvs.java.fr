package cn.staitech.fr.controller;


import cn.staitech.common.core.domain.R;
import cn.staitech.common.log.annotation.Log;
import cn.staitech.common.log.enums.BusinessType;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.service.HistoryService;
import cn.staitech.fr.vo.history.Cursor;
import cn.staitech.fr.vo.history.HistoryDTO;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author: wangfeng
 * @create: 2024-2-20 18:07:38
 * @Description: 颜色
 */
@Api(value = "标注编辑-历史记录", tags = "标注编辑-历史记录")
@RestController
@RequestMapping("/history")
@Slf4j
public class HistoryController {

    @Resource
    HistoryService historyService;

    /**
     * 撤消、恢复 .
     */
    @ApiOperationSupport(author = "wangfeng")
    @ApiOperation(value = "标注编辑-历史记录", notes = "标注编辑-历史记录 - 王峰")
    @Log(title = "标注编辑-历史记录", menu = "标注编辑-历史记录", subMenu = "撤消、恢复", businessType = BusinessType.QUERY)
    @PostMapping("/process")
    public R<String> process(@Validated @RequestBody HistoryDTO dto) {
        if (dto.getUserId() == null) {
            dto.setUserId(SecurityUtils.getLoginUser().getSysUser().getUserId());
        }
        historyService.process(dto);
        return R.ok();
    }

    /**
     * 获取撤消、恢复状态 .
     */
    @ApiOperationSupport(author = "wangfeng")
    @ApiOperation(value = "标注编辑-历史记录", notes = "标注编辑-历史记录 - 王峰")
    @Log(title = "标注编辑-历史记录", menu = "标注编辑-历史记录", subMenu = "获取撤消、恢复状态,游标可移动次数", businessType = BusinessType.QUERY)
    @PostMapping("/index")
    public R<Cursor> index(@Validated @RequestBody HistoryDTO dto) {
        if (dto.getUserId() == null) {
            dto.setUserId(SecurityUtils.getLoginUser().getSysUser().getUserId());
        }
        return R.ok(historyService.getCursor(dto));
    }

    /**
     * 清空
     */
    @ApiOperationSupport(author = "wangfeng")
    @ApiOperation(value = "标注编辑-历史记录", notes = "标注编辑-历史记录 - 王峰")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "slideId", value = "切片ID", required = true, dataType = "Long", paramType = "query")})
    @Log(title = "标注编辑-历史记录", menu = "标注编辑-历史记录", subMenu = "清空", businessType = BusinessType.QUERY)
    @GetMapping("/clean")
    public R<String> clean(@ApiParam(name = "slideId", value = "切片ID", required = true) Long slideId
                           ) {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        historyService.clearSessionList(userId, slideId);
        return R.ok();
    }
}
