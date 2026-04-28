package cn.staitech.fr.controller;

import java.util.List;
import javax.annotation.Resource;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.domain.Topic;
import cn.staitech.fr.service.TopicService;
import cn.staitech.system.api.domain.SysUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @author mugw
 * @version 2.6.0
 * @description 专题管理
 * @date 2025/5/14 13:44:14
 */
@Api(value = "切片列表（原图像管理）", tags = "切片列表（原图像管理）")
@RestController
@RequestMapping("/topic")
@Slf4j
public class TopicController {

    @Resource
    private TopicService topicService;

    @ApiOperation(value = "切片项目列表 - 无分页版")
    @GetMapping("/list")
    public R<List<Topic>> list() {
        Long organizationId = SecurityUtils.getOrganizationId();
        List<Topic> list = topicService.list(Wrappers.<Topic>lambdaQuery()
                .eq(!SysUser.isAdmin(SecurityUtils.getUserId()), Topic::getOrganizationId, organizationId).orderByDesc(Topic::getTopicId));
        return R.ok(list);
    }

}
