package cn.staitech.fr.service.impl;

import cn.staitech.fr.domain.JsonFile;
import cn.staitech.fr.mapper.JsonFileMapper;
import cn.staitech.fr.service.JsonFileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * (JsonFile)表服务实现类
 *
 * @author makejava
 * @since 2024-05-11 13:56:33
 */
@Service("jsonFileService")
public class JsonFileServiceImpl extends ServiceImpl<JsonFileMapper, JsonFile> implements JsonFileService {

}

