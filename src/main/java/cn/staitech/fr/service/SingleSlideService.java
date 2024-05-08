package cn.staitech.fr.service;

import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.OrganDisassemblyQueryIn;
import cn.staitech.fr.domain.out.ImageExportOut;
import cn.staitech.fr.domain.out.OrganDisassemblyOut;
import cn.staitech.fr.domain.out.SingleSlideSelectBy;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SingleSlideService extends IService<SingleSlide> {

    List<OrganDisassemblyOut> getSingleList(OrganDisassemblyQueryIn req);

    List<ImageExportOut> getExportList(List<Long> imageIds);

    SingleSlideSelectBy singleSlideBy(Long singleId);
}
