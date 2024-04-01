package cn.staitech.fr.service;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.in.StartPredictionIn;

/**
 * @author wanglibei
 * @version V1.0
 * @ClassName: AlgorithmPredictionService
 * @Description:算法预测
 * @date 2023年11月2日
 */
public interface AlgorithmPredictionService {

    R startPrediction(StartPredictionIn req);

}
