package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.Color;
import cn.staitech.fr.enums.ColorTypeEnum;
import cn.staitech.fr.service.ColorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author mugw
 * @version 1.0
 * @description 颜色管理
 * @date 2025/5/14 13:44:14
 */
@Api(value = "颜色", tags = {"V2.6.0"})
@RestController
@RequestMapping("/color")
@Slf4j
public class ColorController {

    @Resource
    private ColorService colorService;

    /**
     * 颜色类型列表 .
     */
    @ApiOperation(value = "颜色类型列表")
    @GetMapping("/colorType")
    public R<Map<Integer, String>> colorType() {
        Map<Integer, String> map = ColorTypeEnum.getMap() ;
        return R.ok(map);
    }

    /**
     * 颜色列表 .
     */
    @ApiOperation(value = "颜色类型", tags = {"V2.6.0"})
    @GetMapping("/list")
    public R<List<Color>> list() throws ExecutionException, InterruptedException {
        List<Color> list = colorService.list();
        return R.ok(list);
    }
}
