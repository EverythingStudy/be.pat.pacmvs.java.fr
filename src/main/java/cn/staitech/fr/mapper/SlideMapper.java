package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.domain.in.SlideListQueryIn;
import cn.staitech.fr.domain.out.SlideListQueryOut;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author admin
* @description 针对表【fr_slide(专题选片表)】的数据库操作Mapper
* @createDate 2024-03-29 13:33:37
* @Entity cn.staitech.fr.domain.Slide
*/
public interface SlideMapper extends BaseMapper<Slide> {
    String selectBySpecialId(Long specialId);

    List<SlideListQueryOut> slideListQuery(SlideListQueryIn req);
}




