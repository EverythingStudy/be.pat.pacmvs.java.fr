package cn.staitech.fr.service;


import cn.staitech.fr.domain.ContourJson;
import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.domain.JsonTask;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author admin
* @description 针对表【fr_contour_json】的数据库操作Service
* @createDate 2024-10-15 14:58:30
*/
public interface ContourJsonService extends IService<ContourJson> {


    void aiJson(List<JsonFile> jsonFileList, JsonTask jsonTask);

}
