package cn.staitech.fr.service;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.fr.domain.in.OrganDisassemblyQueryIn;
import cn.staitech.fr.domain.out.OrganDisassemblyOut;

public interface OrganDisassemblyService {
    PageResponse<OrganDisassemblyOut> getList(OrganDisassemblyQueryIn req);
}
