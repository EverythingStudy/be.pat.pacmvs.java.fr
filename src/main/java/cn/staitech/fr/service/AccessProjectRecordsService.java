package cn.staitech.fr.service;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.AccessProjectRecords;
import cn.staitech.fr.vo.AccessProjectRecordsVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 访问项目记录表 服务类
 * </p>
 *
 * @author wanglibei
 * @since 2024-04-15
 */
public interface AccessProjectRecordsService extends IService<AccessProjectRecords> {

    R<List<AccessProjectRecordsVo>> accessProjectStatistics() throws Exception;
}
