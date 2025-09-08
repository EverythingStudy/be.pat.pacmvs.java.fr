package cn.staitech.fr.service;

import cn.staitech.fr.domain.in.AiDownloadIn;
import cn.staitech.fr.vo.project.slide.SlidePageReq;

/**
 * @Author wudi
 * @Date 2024/4/10 15:54
 * @desc
 */
public interface MatrixReviewService {

    void algorithmDownload(SlidePageReq req) throws Exception;

}
