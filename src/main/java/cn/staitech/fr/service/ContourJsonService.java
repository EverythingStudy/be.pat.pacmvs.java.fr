package cn.staitech.fr.service;


import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.ContourJson;
import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.out.ContourFileVo;
import cn.staitech.fr.domain.out.JsonFileVo;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author admin
* @description 针对表【fr_contour_json】的数据库操作Service
* @createDate 2024-10-15 14:58:30
*/
public interface ContourJsonService extends IService<ContourJson> {


    void aiJson(List<JsonFile> jsonFileList, JsonTask jsonTask);

    R<ContourFileVo> getContourJsonSize(Long slideId,Long projectId,List<Long> organTagIds);
    
    R<JsonFileVo> selectList(Long slideId,Long projectId, Long organTagId);
}
