package cn.staitech.fr.controller;

import cn.staitech.common.core.domain.CustomPage;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.OrganTag;
import cn.staitech.fr.vo.OrganTagPageReq;
import cn.staitech.fr.service.OrganTagService;
import cn.staitech.system.api.domain.biz.OrganTagQuery;
import cn.staitech.system.api.domain.biz.OrganTagQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * @author mugw
 * @version 1.0
 * @description 脏器标签管理
 * @date 2025/5/14 13:44:14
 */
@Api(value = "脏器标签", tags = {"V2.6.0"})
@RestController
@RequestMapping("/organTag")
public class OrganTagController {

    @Resource
    private OrganTagService organTagService;

    @ApiOperation(value = "脏器标签表列表分页查询", tags = {"V2.6.0"})
    @PostMapping("/page")
    public R<CustomPage<OrganTag>> page(@RequestBody OrganTagPageReq req) throws ParseException {

        CustomPage<OrganTag> page = new CustomPage<>(req);
        Date beginTime = null;
        Date endTime = null;

        if (req.getCreateTime() != null) {
            beginTime = req.getCreateTime().getBeginTime();
            endTime = req.getCreateTime().getEndTime();
        }

        boolean hasSpeciesId = ObjectUtils.isNotEmpty(req.getSpeciesId());
        boolean hasOrganName = ObjectUtils.isNotEmpty(req.getOrganName());
        boolean hasAbbreviation = ObjectUtils.isNotEmpty(req.getAbbreviation());
        boolean hasBeginTime = ObjectUtils.isNotEmpty(beginTime);
        boolean hasEndTime = ObjectUtils.isNotEmpty(endTime);

        LambdaQueryWrapper<OrganTag> queryWrapper = Wrappers.<OrganTag>lambdaQuery()
                .eq(hasSpeciesId, OrganTag::getSpeciesId, req.getSpeciesId())
                .eq(hasOrganName, OrganTag::getOrganName, req.getOrganName())
                .like(hasAbbreviation, OrganTag::getAbbreviation, req.getAbbreviation())
                .eq(OrganTag::getDelFlag, false)
                .ge(hasBeginTime, OrganTag::getCreateTime, beginTime)
                .le(hasEndTime, OrganTag::getCreateTime, endTime);

        organTagService.page(page, queryWrapper);
        return R.ok(page);
    }



    @ApiOperation(value = "脏器标签表列表查询" , tags = {"V2.6.0"})
    @GetMapping("/list")
    public R<List<OrganTag>> list(String organName) {
        LambdaQueryWrapper<OrganTag> categoryQueryWrapper = new LambdaQueryWrapper<OrganTag>().like(organName != null, OrganTag::getOrganName, organName).eq(OrganTag::getDelFlag, false);
        return R.ok(organTagService.list(categoryQueryWrapper));
    }

    @ApiOperation(value = "查询脏器标签", tags = {"V2.6.0"})
    @PostMapping("/queryOrganTag")
    public R<List<OrganTagQueryVo>> queryOrganTag(@RequestBody OrganTagQuery organTagQuery) {
        List<OrganTagQueryVo> vos = new ArrayList<>();
        List<OrganTag> organTags = this.organTagService.listByIds(organTagQuery.getOrganTagIds());
        if (!CollectionUtils.isEmpty(organTags)) {
            for (OrganTag organTag : organTags) {
                OrganTagQueryVo vo = new OrganTagQueryVo();
                BeanUtils.copyProperties(organTag, vos);
                vos.add(vo);
            }
        }
        return R.ok(vos);
    }
}
