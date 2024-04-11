package cn.staitech.fr.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.SingleOrganNumber;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.domain.Special;
import cn.staitech.fr.domain.in.MatrixReviewEditIn;
import cn.staitech.fr.domain.in.MatrixReviewListIn;
import cn.staitech.fr.domain.out.AnimalDimensionOut;
import cn.staitech.fr.domain.out.MatrixReviewListOut;
import cn.staitech.fr.domain.out.MatrixReviewOut;
import cn.staitech.fr.domain.out.OrganDisassemblyOut;
import cn.staitech.fr.domain.out.WaxBlockNumberListOut;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.mapper.SlideMapper;
import cn.staitech.fr.mapper.SpecialMapper;
import cn.staitech.fr.service.MatrixReviewService;
import cn.staitech.system.api.domain.SysUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author wudi
 * @Date 2024/4/10 15:54
 * @desc
 */
@Service
@Slf4j
public class MatrixReviewServiceImpl implements MatrixReviewService {
    @Resource
    private SlideMapper slideMapper;
    @Autowired
    private SpecialMapper specialMapper;

    @Resource
    private SingleSlideMapper singleSlideMapper;

    @Override
    public R<List<MatrixReviewOut>> groupList(Long specialId) {
        log.info("对照组数据查询接口开始：");
        List<MatrixReviewOut> resp = new ArrayList<>();
        LambdaQueryWrapper<Slide> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Slide::getGroupCode);
        wrapper.eq(Slide::getSpecialId, specialId);
        wrapper.eq(Slide::getDelFlag, CommonConstant.NUMBER_0);
        wrapper.isNotNull(Slide::getGroupCode);
        wrapper.groupBy(Slide::getGroupCode);
        List<Slide> slideList = slideMapper.selectList(wrapper);
        if (CollectionUtils.isNotEmpty(slideList)) {
            resp = slideList.stream().map(e -> {
                MatrixReviewOut matrixReviewOut = new MatrixReviewOut();
                matrixReviewOut.setGroupId(e.getGroupCode());
                matrixReviewOut.setGroupCode(e.getGroupCode());
                return matrixReviewOut;
            }).collect(Collectors.toList());
        }
        return R.ok(resp);
    }

    @Override
    public R edit(MatrixReviewEditIn req) {
        log.info("对照组数据编辑接口开始：");
        Special special = new Special();
        special.setSpecialId(req.getSpecialId());
        special.setControlGroup(req.getGroupId());
        specialMapper.updateById(special);
        return R.ok();
    }

    @Override
    public PageResponse<MatrixReviewListOut> getMatrixReview(MatrixReviewListIn req) {
        log.info("阅片列表单切片维度接口查询开始：");
        //创建响应
        PageResponse resp = new PageResponse();
        //分页查询
        Page<MatrixReviewListOut> page = PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<MatrixReviewListOut> waxList = slideMapper.getMatrixReview(req);
        if (CollectionUtils.isEmpty(waxList)) {
            resp.setTotal(page.getTotal());
            resp.setList(waxList);
            resp.setPages(page.getPages());
            return resp;
        }
        List<Long> slideIds = waxList.stream().map(MatrixReviewListOut::getSlideId).distinct().collect(Collectors.toList());
        List<SingleOrganNumber> singleOrganNumbers = singleSlideMapper.selectNumber(slideIds, req.getCategoryId());
        if (CollectionUtil.isEmpty(singleOrganNumbers)) {
            resp.setTotal(page.getTotal());
            resp.setList(waxList);
            resp.setPages(page.getPages());
            return resp;
        }
        Map<Long, Map<Long, Long>> map = singleOrganNumbers.stream()
                .collect(Collectors.groupingBy(
                        SingleOrganNumber::getSlideId,
                        Collectors.toMap(
                                SingleOrganNumber::getCategoryId, SingleOrganNumber::getOrganNumber
                        )
                ));
        waxList = waxList.stream().peek(p -> p.setOrganNumber(map.get(p.getSlideId()).get(p.getCategoryId())))
                .collect(Collectors.toList());
        resp.setTotal(page.getTotal());
        resp.setList(waxList);
        resp.setPages(page.getPages());
        return resp;
    }

    @Override
    public PageResponse<AnimalDimensionOut> animalList(MatrixReviewListIn req) {
        log.info("阅片列表单动物维度接口查询开始：");
        return null;
    }
}
