package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.SpecialRecycling;
import cn.staitech.fr.domain.in.SpecialRecyclingListQueryIn;
import cn.staitech.fr.domain.out.SpecialRecyclingListQueryOut;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 专题回收站表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2024-03-29
 */
public interface SpecialRecyclingMapper extends BaseMapper<SpecialRecycling> {

    List<SpecialRecyclingListQueryOut> getSpecialRecyclingList(SpecialRecyclingListQueryIn req);
}
