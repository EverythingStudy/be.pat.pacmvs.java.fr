package cn.staitech.fr.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.Special;
import cn.staitech.fr.domain.in.EditSpecialStatusIn;
import cn.staitech.fr.domain.in.SpecialAddIn;
import cn.staitech.fr.domain.in.SpecialEditIn;
import cn.staitech.fr.domain.in.SpecialListQueryIn;
import cn.staitech.fr.domain.in.SpecialsQueryIn;
import cn.staitech.fr.domain.out.SpecialListQueryOut;
import cn.staitech.system.api.domain.SysUser;

/**
 * <p>
 * 专题表 服务类
 * </p>
 *
 * @author author
 * @since 2024-03-29
 */
public interface SpecialService extends IService<Special> {

    PageResponse<SpecialListQueryOut> getSpecialList(SpecialListQueryIn req);

    R addSpecial(SpecialAddIn req);

    R editSpecial(SpecialEditIn req);

    R removeSpecial(Long specialId);

    R editSpecialStatus(EditSpecialStatusIn req);

    PageResponse<SpecialListQueryOut> getSpecials(SpecialsQueryIn req);

    R<Special> getInfoById(Long specialId);
    
    public SysUser getUserInfo(Map<String,Object> parm);
}
