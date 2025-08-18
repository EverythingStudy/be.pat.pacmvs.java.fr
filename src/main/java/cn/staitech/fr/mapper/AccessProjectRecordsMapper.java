package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.AccessProjectRecords;
import cn.staitech.fr.vo.AccessProjectRecordsVo;
import cn.staitech.fr.vo.project.AccessProjectRecordReq;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 访问项目记录表 Mapper 接口
 * </p>
 *
 * @author wanglibei
 * @since 2024-04-15
 */
public interface AccessProjectRecordsMapper extends BaseMapper<AccessProjectRecords> {

    List<AccessProjectRecordsVo> accessProjectStatistics(AccessProjectRecordReq req);
}
