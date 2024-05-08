package cn.staitech.fr.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import cn.staitech.fr.domain.Organ;

/**
 * @author: wangfeng
 * @create: 2023-09-10 13:08:31
 * @Description: 脏器Mapper
 */

public interface OrganMapper extends BaseMapper<Organ> {
    List<Organ> selectList();

    List<Organ> getOrganBySpeciesId(Organ organ);
}
