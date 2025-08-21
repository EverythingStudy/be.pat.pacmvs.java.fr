package cn.staitech.fr.service;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.AccessViewRecords;
import cn.staitech.fr.vo.project.AccessViewStatisticsReq;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 访问view页面次数记录首页日活 服务类
 * </p>
 *
 * @author jiazx
 * @since 2025-08-19
 */
public interface AccessViewRecordsService extends IService<AccessViewRecords> {

    void saveAccessViewRecords(Long slideId);

    R accessViewStatistics(AccessViewStatisticsReq req);
}
