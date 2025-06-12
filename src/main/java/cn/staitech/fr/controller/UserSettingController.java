package cn.staitech.fr.controller;

import cn.hutool.core.date.DateUtil;
import cn.staitech.common.core.domain.R;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.domain.UserSetting;
import cn.staitech.fr.service.UserSettingService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author mugw
 * @version 1.0
 * @description 个人偏好设置
 * @date 2025/5/14 13:44:14
 */
@Api(value = "个人偏好设置", tags = {"V2.6.0"})
@RestController
@RequestMapping("/user-setting")
public class UserSettingController {

    @Resource
    private UserSettingService userSettingService;

    /**
     * 新增个人偏好设置
     */
    @ApiOperation(value = "新增个人偏好设置", tags = {"V2.6.0"})
    @PostMapping("/add")
    public R add(@RequestBody UserSetting userSetting) throws Exception{
        userSetting.setUserId(SecurityUtils.getUserId());
        userSettingService.save(userSetting);
        return R.ok();
    }

    @ApiOperation(value = "更新个人偏好设置", tags = {"V2.6.0"})
    @PostMapping("/update")
    public R update(@RequestBody UserSetting userSetting) throws Exception{
        userSetting.setUpdateBy(SecurityUtils.getUserId());
        userSetting.setUpdateTime(DateUtil.date());
        userSettingService.updateById(userSetting);
        return R.ok();
    }
    @ApiOperation(value = "查询个人偏好设置", tags = {"V2.6.0"})
    @GetMapping("/get")
    public R<UserSetting> get() throws Exception{
        UserSetting userSetting = userSettingService.getOne(Wrappers.<UserSetting>lambdaQuery().eq(UserSetting::getUserId, SecurityUtils.getUserId()));
        return R.ok(userSetting);
    }
}
