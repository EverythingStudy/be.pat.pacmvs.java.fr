package cn.staitech.fr.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import cn.staitech.fr.domain.Structure;

/**
 * @author: wangfeng
 * @create: 2023-09-10 13:08:31
 * @Description: 结构Mapper
 */

public interface StructureMapper extends BaseMapper<Structure> {
    List<Structure> selectList(Structure structure);
}
