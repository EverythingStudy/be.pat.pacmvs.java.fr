package cn.staitech.fr.service;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.domain.in.ChoiceSaveInVo;
import cn.staitech.fr.domain.in.SlideListQueryIn;
import cn.staitech.fr.domain.out.SlideListQueryOut;
import cn.staitech.fr.domain.out.SlideSelectBy;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author admin
* @description 针对表【fr_slide(专题选片表)】的数据库操作Service
* @createDate 2024-03-29 13:33:37
*/
public interface SlideService extends IService<Slide> {

    R choiceSave(ChoiceSaveInVo choiceSaveInVo);

    PageResponse<SlideListQueryOut> slideListQuery(SlideListQueryIn req);

    /**
     * 查询切片、图片信息接口
     *
     * @param slideId 切片id
     * @return
     */
    SlideSelectBy pageImageCsvListVOBy(Long slideId);

    R deleteById(Long slideId);
}
