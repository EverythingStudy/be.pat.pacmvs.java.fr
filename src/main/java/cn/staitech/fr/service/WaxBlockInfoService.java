package cn.staitech.fr.service;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.WaxBlockInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 蜡块编号明细 服务类
 * </p>
 *
 * @author author
 * @since 2024-03-29
 */
public interface WaxBlockInfoService extends IService<WaxBlockInfo> {

    R<List<WaxBlockInfo>> waxPreview(Long id);
    
    List<WaxBlockInfo> getWaxBlockInfoList (Long slideId,String waxCode,String genderFlag);
    
    List<WaxBlockInfo> getSpecialWaxBlockInfoList (Long specialId);
}
