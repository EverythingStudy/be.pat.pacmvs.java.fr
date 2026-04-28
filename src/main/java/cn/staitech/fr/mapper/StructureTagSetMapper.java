package cn.staitech.fr.mapper;

import cn.staitech.common.core.domain.CustomPage;
import cn.staitech.fr.domain.StructureTagSet;
import cn.staitech.fr.vo.structure.StructureTagSetPageReq;
import cn.staitech.fr.vo.structure.StructureTagSetVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 86186
* @description 针对表【tb_structure_tag_set(结构标签集)】的数据库操作Mapper
* @createDate 2025-05-30 17:01:39
* @Entity cn.staitech.fr.domain.StructureTagSet
*/
public interface StructureTagSetMapper extends BaseMapper<StructureTagSet> {
    CustomPage<StructureTagSetVo> pageStructureTagSet(CustomPage page, @Param("params") StructureTagSetPageReq params);
    List<StructureTagSetVo> queryStructureTagSet(@Param("params") StructureTagSetPageReq params);

}




