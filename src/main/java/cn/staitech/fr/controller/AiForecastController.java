package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.AiForecast;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.utils.MessageSource;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;


@Api(value = "ai预测结果", tags = "ai预测结果")
@Slf4j
@RestController
@RequestMapping("/aiForecast")
public class AiForecastController {

    @Resource
    private AiForecastService aiForecastService;

    @ApiOperation(value = "获取ai预测列表")
    @GetMapping("/selectList")
    public R<List<AiForecast>> list(@RequestParam(value = "singleSlideId") @ApiParam(name = "singleSlideId", value = "单切片ID", required = true) Long singleSlideId) throws Exception {
        if (!Optional.ofNullable(singleSlideId).isPresent()) {
            return R.fail(MessageSource.M("ARGUMENT_INVALID"));
        }
        QueryWrapper<AiForecast> aiForecastQueryWrapper = new QueryWrapper<>();
        aiForecastQueryWrapper.eq("single_slide_id", singleSlideId);
        return R.ok(aiForecastService.list(aiForecastQueryWrapper));
    }



}
