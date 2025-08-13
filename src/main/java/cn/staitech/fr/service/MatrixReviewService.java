package cn.staitech.fr.service;

import cn.staitech.fr.domain.in.AiDownloadIn;

/**
 * @Author wudi
 * @Date 2024/4/10 15:54
 * @desc
 */
public interface MatrixReviewService {

    void algorithmDownload(AiDownloadIn req) throws Exception;

}
