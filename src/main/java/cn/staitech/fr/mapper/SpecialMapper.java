package cn.staitech.fr.mapper;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import cn.staitech.fr.domain.Special;
import cn.staitech.fr.domain.in.SpecialListQueryIn;
import cn.staitech.fr.domain.in.SpecialsQueryIn;
import cn.staitech.fr.domain.out.SpecialListQueryOut;
import cn.staitech.system.api.domain.SysUser;

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

    List<SpecialListQueryOut> getSpecials(SpecialsQueryIn req);

    Integer countgetUserRole(Long userId);
    
    List<SysUser>  selectUserById(Map<String,Object> parm);
}
