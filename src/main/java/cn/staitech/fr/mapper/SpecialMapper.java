package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.Special;
import cn.staitech.fr.domain.in.SpecialListQueryIn;
import cn.staitech.fr.domain.out.SpecialListQueryOut;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 专题表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2024-03-29
 */
public interface SpecialMapper extends BaseMapper<Special> {

    List<SpecialListQueryOut> getSpecialList(SpecialListQueryIn req);
}
