package cn.staitech.fr.service.impl;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.domain.Special;
import cn.staitech.fr.domain.in.MatrixReviewEditIn;
import cn.staitech.fr.domain.in.MatrixReviewListIn;
import cn.staitech.fr.domain.out.MatrixReviewListOut;
import cn.staitech.fr.domain.out.MatrixReviewOut;
import cn.staitech.fr.mapper.SlideMapper;
import cn.staitech.fr.mapper.SpecialMapper;
import cn.staitech.fr.service.MatrixReviewService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
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

        return null;
    }
}
