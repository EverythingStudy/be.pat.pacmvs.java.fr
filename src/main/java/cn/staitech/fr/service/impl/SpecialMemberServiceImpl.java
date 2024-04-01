package cn.staitech.fr.service.impl;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.constant.CommonConstant;
import cn.staitech.fr.domain.SpecialMember;
import cn.staitech.fr.domain.in.SpecialMemberSelectIn;
import cn.staitech.fr.domain.out.SpecialMemberSelectOut;
import cn.staitech.fr.mapper.SpecialMemberMapper;
import cn.staitech.fr.service.SpecialMemberService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 专题成员表 服务实现类
 * </p>
 *
 * @author author
 * @since 2024-03-29
 */
@Service
@Slf4j
public class SpecialMemberServiceImpl extends ServiceImpl<SpecialMemberMapper, SpecialMember> implements SpecialMemberService {

    @Override
    public PageResponse<SpecialMemberSelectOut> getSpecialMemberList(SpecialMemberSelectIn req) {
        log.info("专题用户列表查询接口开始：");
        //创建响应
        PageResponse resp = new PageResponse();
        //分页查询
        Page<SpecialMemberSelectOut> page = PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<SpecialMemberSelectOut> specialList = this.baseMapper.getSpecialMemberList(req);
        resp.setTotal(page.getTotal());
        resp.setList(specialList);
        resp.setPages(page.getPages());
        return resp;

    }

    @Override
    public R removeMember(Long memberId) {
        log.info("专题成员删除接口开始：");
        SpecialMember specialMember = new SpecialMember();
        specialMember.setMemberId(memberId);
        specialMember.setDelFlag(CommonConstant.NUMBER_1);
        this.updateById(specialMember);
        return R.ok();

    }
}
