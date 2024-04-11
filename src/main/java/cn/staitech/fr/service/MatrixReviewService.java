package cn.staitech.fr.service;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.in.MatrixReviewEditIn;
import cn.staitech.fr.domain.in.MatrixReviewListIn;
import cn.staitech.fr.domain.out.MatrixReviewListOut;
import cn.staitech.fr.domain.out.MatrixReviewOut;

import java.util.List;

/**
 * @Author wudi
 * @Date 2024/4/10 15:54
 * @desc
 */
public interface MatrixReviewService {
    R<List<MatrixReviewOut>> groupList(Long specialId);

    R edit(MatrixReviewEditIn req);

    PageResponse<MatrixReviewListOut> getMatrixReview(MatrixReviewListIn req);
}
