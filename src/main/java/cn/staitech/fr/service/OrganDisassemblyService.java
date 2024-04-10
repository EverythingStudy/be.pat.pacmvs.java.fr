package cn.staitech.fr.service;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.fr.domain.in.ImageVagueQueryIn;
import cn.staitech.fr.domain.in.OrganDisassemblyQueryIn;
import cn.staitech.fr.domain.out.ImageVagueListOutVO;
import cn.staitech.fr.domain.out.OrganDisassemblyOut;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface OrganDisassemblyService {
    PageResponse<OrganDisassemblyOut> getList(OrganDisassemblyQueryIn req);

    void export(List<Long> imageIds) throws IOException;

}
