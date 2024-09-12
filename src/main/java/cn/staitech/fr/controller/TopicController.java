package cn.staitech.fr.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import cn.staitech.common.core.domain.R;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.domain.Topic;
import cn.staitech.fr.service.TopicService;
import cn.staitech.system.api.domain.SysUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;


/**
 * @author: wangfeng
 * @create: 2023-06-02 14:19:22
 * @Description: 切片（原图像）专题
 */
@Api(value = "切片列表（原图像管理）", tags = "切片列表（原图像管理）")
@RestController
@RequestMapping("/topic")
@Slf4j
public class TopicController {

    @Resource
    private TopicService topicService;

    @ApiOperation(value = "切片专题列表 - 无分页版")
    @GetMapping("/list")
    public R<List<Topic>> list() {
        // 组织ID
        Long organizationId = SecurityUtils.getLoginUser().getSysUser().getOrganizationId();
        QueryWrapper<Topic> qw = new QueryWrapper();
        qw.eq(!SysUser.isAdmin(SecurityUtils.getUserId()), "organization_id", organizationId);
        qw.orderByDesc("topic_id");
        List<Topic> list = topicService.list(qw);
        return R.ok(list);
    }


}
