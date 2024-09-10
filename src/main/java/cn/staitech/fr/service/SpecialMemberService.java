package cn.staitech.fr.service;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.SpecialMember;
import cn.staitech.fr.domain.in.AddMemberIn;
import cn.staitech.fr.domain.in.SpecialMemberSelectIn;
import cn.staitech.fr.domain.out.SpecialMemberSelectOut;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 专题成员表 服务类
 * </p>
 *
 * @author author
 * @since 2024-03-29
 */
public interface SpecialMemberService extends IService<SpecialMember> {

    PageResponse<SpecialMemberSelectOut> getSpecialMemberList(SpecialMemberSelectIn req);

    R removeMember(Long memberId);

    R addMember(AddMemberIn req);
}
