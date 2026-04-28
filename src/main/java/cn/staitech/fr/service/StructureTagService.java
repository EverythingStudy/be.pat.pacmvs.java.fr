package cn.staitech.fr.service;

import cn.staitech.common.core.domain.CustomPage;
import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.StructureTag;
import cn.staitech.fr.vo.structure.StructureTagPageReq;
import cn.staitech.fr.vo.structure.StructureTagPageVo;
import cn.staitech.fr.vo.structure.StructureTagVo;
import com.baomidou.mybatisplus.extension.service.IService;


public interface StructureTagService extends IService<StructureTag> {
    R<StructureTag> addTag(StructureTagVo req) throws Exception;
    R<StructureTag> editTag(StructureTagVo req)throws Exception;
    R<CustomPage<StructureTagPageVo>> pageTag(StructureTagPageReq req) throws Exception;
    R delTag(Long structureTagId)throws Exception;
}
