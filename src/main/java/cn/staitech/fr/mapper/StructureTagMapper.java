package cn.staitech.fr.mapper;

import cn.staitech.common.core.domain.CustomPage;
import cn.staitech.fr.domain.StructureTag;
import cn.staitech.fr.vo.structure.StructureTagPageReq;
import cn.staitech.fr.vo.structure.StructureTagPageVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StructureTagMapper extends BaseMapper<StructureTag> {

    CustomPage<StructureTagPageVo> pageTag(CustomPage page,@Param("params") StructureTagPageReq req);
    List<StructureTagPageVo> queryTag(@Param("params") StructureTagPageReq req);
}




