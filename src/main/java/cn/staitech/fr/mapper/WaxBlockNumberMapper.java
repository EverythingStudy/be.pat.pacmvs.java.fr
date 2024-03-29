package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.WaxBlockNumber;
import cn.staitech.fr.domain.in.WaxBlockNumberListIn;
import cn.staitech.fr.domain.out.WaxBlockNumberListOut;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 蜡块编号表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2024-03-28
 */
public interface WaxBlockNumberMapper extends BaseMapper<WaxBlockNumber> {

    List<WaxBlockNumberListOut> getWaxList(WaxBlockNumberListIn req);
}
