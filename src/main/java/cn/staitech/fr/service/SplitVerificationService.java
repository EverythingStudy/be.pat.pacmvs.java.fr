package cn.staitech.fr.service;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.fr.domain.in.ResultCorrectionIn;
import cn.staitech.fr.domain.in.SplitVerificationQueryIn;
import cn.staitech.fr.domain.out.SplitVerificationOut;

public interface SplitVerificationService {
    PageResponse<SplitVerificationOut> getList(SplitVerificationQueryIn req);
    void updateResult(ResultCorrectionIn req);
}
