package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.R;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.service.ProductionService;
import cn.staitech.fr.vo.project.ProductionReq;
import cn.staitech.fr.vo.project.ProductionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 制片信息
 *
 * @author yxy
 */
@Api(value = "制片信息", tags = "制片信息")
@RestController
@RequestMapping("/production")
@Slf4j
public class ProductionController {

    @Resource
    private ProductionService productionService;

    @ApiOperation(value = "制片信息列表")
    @PostMapping("/list")
    public R<List<ProductionVO>> list(@RequestBody @Validated ProductionReq req) {
        Long organizationId = SecurityUtils.getOrganizationId();
        req.setOrganizationId(organizationId);
        List<ProductionVO> list = this.productionService.list(req);
        return R.ok(list);
    }

    @ApiOperation(value = "蜡块编号下拉列表")
    @PostMapping("/waxCodeList")
    public R<List<String>> waxCodeList(@RequestBody @Validated ProductionReq req) {
        Long organizationId = SecurityUtils.getOrganizationId();
        req.setOrganizationId(organizationId);
        List<String> list = this.productionService.waxCodeList(req);
        return R.ok(list);
    }
}
