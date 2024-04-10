package cn.staitech.fr.service;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.SpecialRecycling;
import cn.staitech.fr.domain.in.SpecialRecyclingListQueryIn;
import cn.staitech.fr.domain.in.SpecialRecyclingRecoverIn;
import cn.staitech.fr.domain.out.SpecialRecyclingListQueryOut;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 专题回收站表 服务类
 * </p>
 *
 * @author author
 * @since 2024-03-29
 */
public interface SpecialRecyclingService extends IService<SpecialRecycling> {

    PageResponse<SpecialRecyclingListQueryOut> getSpecialRecyclingList(SpecialRecyclingListQueryIn req);

    R recoverSpecial(SpecialRecyclingRecoverIn req);
}
