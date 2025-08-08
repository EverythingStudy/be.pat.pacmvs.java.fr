package cn.staitech.fr.service;

import cn.staitech.fr.domain.Organ;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
* @author 86186
* @description 针对表【tb_organ】的数据库操作Service
* @createDate 2025-05-30 17:01:38
*/
public interface OrganService extends IService<Organ> {
    Map<String, String> getCategory();
}
