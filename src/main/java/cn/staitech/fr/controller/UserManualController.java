package cn.staitech.fr.controller;


import cn.staitech.common.core.domain.R;
import cn.staitech.common.redis.service.RedisService;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.domain.UserManual;
import cn.staitech.fr.service.UserManualService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author wanglibei
 * @version V1.0
 * @ClassName: UserManualController
 * @Description:用户手册
 * @date 2024年1月4日
 */
@Api(value = "用户手册", tags = "用户手册")
@RestController
@RequestMapping("/userManual")
public class UserManualController {


    @Resource
    private UserManualService uerManualService;

    @Resource
    private RedisService redisService;

    /**
     * 用户手册 .
     */
    @ApiOperation(value = "获取用户手册", notes = "获取用户手册")
    @GetMapping("/getUserManual")
    public R<UserManual> getUserManual() {
        //获取语言
        String language = "";
        if (SecurityUtils.getLoginUser().getLanguage() != null) {
            language = SecurityUtils.getLoginUser().getLanguage();
        }
        int messagesType = 1;
        if ("en-us".equals(language)) {
            messagesType = 2;
        }

        //获取机构id
        Long organizationId = SecurityUtils.getLoginUser().getSysUser().getOrganizationId();
        String cacheKey = "userManual_" + organizationId + "_" + messagesType;
        UserManual userManual = redisService.getCacheObject(cacheKey);
        if (null == userManual) {
            QueryWrapper<UserManual> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("organization_id", organizationId);
            queryWrapper.eq("messages_type", messagesType);
            queryWrapper.eq("del_flag", 0);
            userManual = uerManualService.getOne(queryWrapper);
            redisService.setCacheObject(cacheKey, userManual, 24 * 31L, TimeUnit.HOURS);
        }

        return R.ok(userManual);
    }
    
}
