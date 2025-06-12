package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.CustomPage;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.Image;
import cn.staitech.fr.service.ImageService;
import cn.staitech.fr.utils.MessageSource;
import cn.staitech.fr.vo.image.ImageStatusVo;
import cn.staitech.fr.vo.image.ImageBatchIdsVO;
import cn.staitech.fr.vo.image.ImagePageReq;
import cn.staitech.fr.vo.image.ImageUpdateVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.text.ParseException;
import java.util.List;


@Api(value = "切片管理", tags = "切片管理")
@RestController
@RequestMapping("/image")
public class ImageController {

    @Resource
    private ImageService imageService;

    /**
     * 切片状态列表 .
     */
    @ApiOperation(value = "切片状态列表", notes = "切片状态列表")
    @PostMapping("/status")
    public R<List<ImageStatusVo>> status() {
        return R.ok(imageService.status());
    }


    /**
     * 获取原始切片的分页列表
     *
     * @param req 包含用于查找切片的搜索条件
     * @return 包含原始切片列表的分页响应
     */
    @ApiOperation(value = "原始切片列表", notes = "原始切片列表")
    @PostMapping("/list")
    public R<CustomPage<Image>> pageImage(@RequestBody ImagePageReq req) throws ParseException {
        return R.ok(imageService.pageImage(req));
    }

    /**
     * 批量删除切片
     *
     * @param request 包含要删除的切片ID列表
     * @return 包含已删除切片ID列表的响应
     * @throws Exception 如果删除过程中发生错误
     */
    @ApiOperation(value = "批量删除切片-物理删除")
    @PostMapping("/deleteBatchIds")
    public R<List<Long>> deleteBatchIds(@Validated @RequestBody ImageBatchIdsVO request) {
        return R.ok(imageService.deleteBatchIds(request));
    }

    /**
     * 更新切片信息
     *
     * @param request 包含要更新的切片信息
     * @return 表示更新操作结果的响应
     * @throws Exception 如果更新过程中发生错误
     */
    @ApiOperation(value = "编辑切片信息")
    @PostMapping("/update")
    public R update(@Validated @RequestBody ImageUpdateVO request) {
        int result = imageService.updateById(request);
        if (result > 0) {
            return R.ok(null, MessageSource.M("OPERATE_SUCCEED"));
        }
        return R.fail(MessageSource.M("OPERATE_ERROR"));
    }

}
