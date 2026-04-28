package cn.staitech.fr.service;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.StructureTagSet;
import cn.staitech.fr.vo.structure.StructureTagSetVo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.bind.annotation.PathVariable;

import javax.validation.constraints.NotNull;

/**
* @author 86186
* @description 针对表【tb_structure_tag_set(结构标签集)】的数据库操作Service
* @createDate 2025-05-30 17:01:39
*/
public interface StructureTagSetService extends IService<StructureTagSet> {
    R<StructureTagSet> addTagSet(StructureTagSetVo req) throws Exception;
    R<StructureTagSet> updateTagSet(StructureTagSetVo req) throws Exception;

    R<String> delTagSet(@PathVariable @NotNull Long id)throws Exception;
    boolean isBoundProject(String speciesId);
}
