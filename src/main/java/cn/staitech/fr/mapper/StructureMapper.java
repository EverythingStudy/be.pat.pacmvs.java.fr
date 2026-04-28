package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.Structure;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author 86186
* @description 针对表【tb_structure】的数据库操作Mapper
* @createDate 2025-05-30 17:01:39
* @Entity cn.staitech.fr.domain.Structure
*/
public interface StructureMapper extends BaseMapper<Structure> {
    List<Structure> queryList(Structure structure);
}




