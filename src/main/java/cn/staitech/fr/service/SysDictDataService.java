package cn.staitech.fr.service;

import java.util.List;
import java.util.Map;

import cn.staitech.fr.domain.SysDictData;
import cn.staitech.fr.vo.diagnosis.SysDictDataVo;

/**
 * 
* @ClassName: SysDictDataService
* @Description:系统字典处理
* @author wanglibei
* @date 2023年6月28日
* @version V1.0
 */
public interface SysDictDataService
{

    public List<SysDictData> getSysDictDataListByParm(Map<String,Object> map);
    
    public SysDictData getMaxDictSortByParm(Map<String,Object> map);
    
    public String saveSysDictDataByParm(String dictValueCn,String dictValueEn,String dictType,String filter);

    
    public List<SysDictDataVo> getSysDictDataVoListByParm(Map<String,Object> map);
}
