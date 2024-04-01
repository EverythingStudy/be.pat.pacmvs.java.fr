package cn.staitech.fr.service;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.domain.in.ChoiceSaveInVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author admin
* @description 针对表【fr_slide(专题选片表)】的数据库操作Service
* @createDate 2024-03-29 13:33:37
*/
public interface SlideService extends IService<Slide> {

    R choiceSave(ChoiceSaveInVo choiceSaveInVo);
}
