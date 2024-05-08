package cn.staitech.fr.mapper;


import java.util.List;
import java.util.Map;

import cn.staitech.fr.domain.SysDictData;
import cn.staitech.fr.vo.diagnosis.SysDictDataVo;
import cn.staitech.fr.vo.diagnosis.SysDictTagVo;

public interface SysDictDataMapper {
    int deleteByPrimaryKey(Long dictCode);

    int insert(SysDictData record);

    int insertSelective(SysDictData record);

    SysDictData selectByPrimaryKey(Long dictCode);

    int updateByPrimaryKeySelective(SysDictData record);

    int updateByPrimaryKey(SysDictData record);
    
    public List<SysDictData> getSysDictDataListByParm(Map<String,Object> map);
    
    public SysDictData getMaxDictSortByParm(Map<String,Object> map);
    
    public List<SysDictDataVo> getSysDictDataVoListByParm(Map<String,Object> map);
    
    public SysDictData getLabelNameByParm(SysDictTagVo sysDictTagVo);
}