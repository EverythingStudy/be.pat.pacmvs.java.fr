package cn.staitech.fr.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.staitech.fr.mapper.JsonTaskMapper;
import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.service.JsonTaskService;
import org.springframework.stereotype.Service;

/**
 * (JsonTask)表服务实现类
 *
 * @author makejava
 * @since 2024-05-10 14:57:01
 */
@Service("jsonTaskService")
public class JsonTaskServiceImpl extends ServiceImpl<JsonTaskMapper, JsonTask> implements JsonTaskService {

}

