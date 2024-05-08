package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.Topic;
import cn.staitech.fr.service.TopicService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author wudi
 * @Date 2024/3/29 15:37
 * @desc
 */
@Api(value = "专题编号", tags = "专题编号")
@RestController
@RequestMapping("/topic")
public class TopicController {
    @Resource
    private TopicService topicService;

    @ApiOperation(value = "切片专题列表 - 无分页版")
    @GetMapping("/list")
    public R<List<Topic>> list(@RequestParam("projectTypeId") @ApiParam(name = "projectTypeId", value = "项目类型ID", required = true) Long projectTypeId) {
        // 组织ID
        return topicService.getTopicList();
    }

}
