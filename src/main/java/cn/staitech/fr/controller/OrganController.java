package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.R;
import cn.staitech.common.core.web.controller.BaseController;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.domain.Organ;
import cn.staitech.fr.service.OrganService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author mugw
 * @version 1.0
 * @description 脏器标签管理
 * @date 2025/5/14 13:44:14
 */
@Api(value = "脏器", tags = {"V2.6.0"})
@RestController
@RequestMapping("/organ")
@Slf4j
public class OrganController extends BaseController {
    @Resource
    private OrganService organService;

    @ApiOperation(value = "脏器列表")
    @GetMapping("/list")
    public R<List<Organ>> list() throws ExecutionException, InterruptedException {
        LambdaQueryWrapper<Organ> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Organ::getOrganizationId, SecurityUtils.getOrganizationId());

        List<Organ> list = organService.list(queryWrapper);
        return R.ok(list);
    }

}
