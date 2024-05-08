package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.SpecialMember;
import cn.staitech.fr.domain.in.SpecialMemberSelectIn;
import cn.staitech.fr.domain.out.SpecialMemberSelectOut;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 专题成员表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2024-03-29
 */
public interface SpecialMemberMapper extends BaseMapper<SpecialMember> {

    List<SpecialMemberSelectOut> getSpecialMemberList(SpecialMemberSelectIn req);
}
