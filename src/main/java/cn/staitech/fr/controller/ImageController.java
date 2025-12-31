package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.CustomPage;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.Image;
import cn.staitech.fr.service.ImageService;
import cn.staitech.fr.utils.MessageSource;
import cn.staitech.fr.vo.image.*;
import cn.staitech.sft.logaudit.annotation.EncryptResponse;
import cn.staitech.sft.logaudit.annotation.LogAudit;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


@Api(value = "切片管理", tags = "切片管理")
@RestController
@RequestMapping("/image")
public class ImageController {

    @Resource
    private ImageService imageService;

    @LogAudit
    @ApiOperation(value = "日志" ,tags = {"I18n"})
    @PostMapping("/addLog")
    public R addLog(ImageLogDetailReq req) {
        return R.ok();
    }

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
    @LogAudit
    @ApiOperation(value = "批量删除切片-物理删除",tags = {"I18n"})
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
    @LogAudit
    @ApiOperation(value = "编辑切片信息" ,tags = {"I18n"})
    @PostMapping("/update")
    public R update(@Validated @RequestBody ImageUpdateVO request) {
        int result = imageService.updateById(request);
        if (result > 0) {
            return R.ok(null, MessageSource.M("OPERATE_SUCCEED"));
        }
        return R.fail(MessageSource.M("OPERATE_ERROR"));
    }

    @EncryptResponse
    @ApiOperation(value = "批量获取切片信息详情", tags = {"I18n"})
    @PostMapping("/detailByIds")
    public R<List<ImageLogDetail>> detailByIds(@RequestBody List<Long> ids) {
        // 参数验证
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException();
        }

        // 验证所有ID都为正数
        for (Long id : ids) {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException(MessageSource.M("INVALID_ID"));
            }
        }

        // 批量查询切片信息
        List<Image> images = imageService.listByIds(ids);
        List<ImageLogDetail> imageDetails = new ArrayList<>();

        for (Image image : images) {
            ImageLogDetail detail = ImageLogDetail.builder()
                    .imageName(image.getImageName())
                    .topicName(image.getTopicName())
                    .size(image.getSize())
                    .organizationId(image.getOrganizationId())
                    .createTime(image.getCreateTime())
                    .status(image.getStatus())
                    .analyzeStatus(image.getAnalyzeStatus())
                    .build();
            imageDetails.add(detail);
        }

        // 补全未查询到的ID，保持返回结果顺序与请求ID顺序一致
        List<ImageLogDetail> result = new ArrayList<>();
        for (Long id : ids) {
            ImageLogDetail found = imageDetails.stream()
                    .filter(detail -> detail.getImageId() != null && detail.getImageId().equals(id))
                    .findFirst()
                    .orElse(ImageLogDetail.builder().build());
            result.add(found);
        }

        return R.ok(result);
    }


}
