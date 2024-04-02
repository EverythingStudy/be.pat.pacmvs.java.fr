package cn.staitech.fr.service.impl;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.fr.domain.in.OrganDisassemblyQueryIn;
import cn.staitech.fr.domain.out.OrganDisassemblyOut;
import cn.staitech.fr.service.OrganDisassemblyService;
import cn.staitech.fr.service.SingleSlideService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author wmy
 * @version 1.0
 * @date 2024/4/2 11:36
 * @description
 */
@Slf4j
@Service
public class OrganDisassemblyServiceImpl implements OrganDisassemblyService {
    @Resource
    private SingleSlideService singleSlideService;

    @Override
    public PageResponse<OrganDisassemblyOut> getList(OrganDisassemblyQueryIn req) {
        PageResponse<OrganDisassemblyOut> pageResponse = new PageResponse<>();
        Page<OrganDisassemblyOut> page = PageHelper.startPage(req.getPageNum(), req.getPageSize());
        // 根据查询条件得到脏器拆分后的数据
        List<OrganDisassemblyOut> disassembly = singleSlideService.getSingleList(req);
        pageResponse.setTotal(page.getTotal());
        pageResponse.setList(disassembly);
        pageResponse.setPages(page.getPages());
        return pageResponse;
    }

}
