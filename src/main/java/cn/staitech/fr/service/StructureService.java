package cn.staitech.fr.service;

import cn.staitech.fr.domain.Structure;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
* @author 86186
* @description 针对表【tb_structure】的数据库操作Service
* @createDate 2025-05-30 17:01:39
*/
public interface StructureService extends IService<Structure> {
    Map<String, Integer> selectStructureSizeMap();
}
