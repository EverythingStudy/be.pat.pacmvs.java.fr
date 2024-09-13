package cn.staitech.fr.service;

import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.out.ImageExportOut;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SingleSlideService extends IService<SingleSlide> {

    List<ImageExportOut> getExportList(List<Long> imageIds);
}
