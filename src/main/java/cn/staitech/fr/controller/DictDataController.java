package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.DictData;
import cn.staitech.fr.service.DictDataService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author wudi
 * @Date 2024/4/11 15:22
 * @desc
 */
@Api(value = "字典数据通用接口", tags = "字典数据通用接口")
@RestController
@RequestMapping("/dictData")
public class DictDataController {

    @Autowired
    private DictDataService dictDataService;

    @ApiOperation(value = "根据codeType获得字典数据")
    @GetMapping(value = "/codeType")
    public R<List<DictData>> dictType(@RequestParam(value = "codeType")
                                    @ApiParam(name="codeType",value = "字典type",required = true) String codeType) {
        LambdaQueryWrapper<DictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(DictData::getDictValue,DictData::getDictLabel,DictData::getDictLabelEn);
        wrapper.eq(DictData::getDictType,codeType);
        List<DictData> list = dictDataService.list(wrapper);
        return R.ok(list);

    }
}
