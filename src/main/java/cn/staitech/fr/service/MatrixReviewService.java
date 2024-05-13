package cn.staitech.fr.service;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.PageDataResponse;
import cn.staitech.fr.domain.in.AiDownloadIn;
import cn.staitech.fr.domain.in.AlgorithmIn;
import cn.staitech.fr.domain.in.MatrixReviewEditIn;
import cn.staitech.fr.domain.in.MatrixReviewListIn;
import cn.staitech.fr.domain.in.SingleSlideAdjacent;
import cn.staitech.fr.domain.in.StartPredictionIn;
import cn.staitech.fr.domain.out.*;

import java.io.IOException;
import java.util.HashMap;
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

    HashMap<String, SingleSlideSelectBy> SingleSlideAdjacent(SingleSlideAdjacent req);

    List<SingleSlideSelectBy> specialSlideList(SingleSlideAdjacent req);

    PageResponse<SelectImageSlideOut> selectSlideList(MatrixReviewListIn req);

    PageDataResponse<AnimalDimensionOut> animalList(MatrixReviewListIn req);

     void diagnosisDownload(AiDownloadIn req) throws Exception;

    R<String> getControlGroup(Long specialId);

    void algorithmDownload(AiDownloadIn req) throws Exception;
    
    R algorithm(AlgorithmIn req);
}
