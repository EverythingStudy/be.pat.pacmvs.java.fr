package cn.staitech.fr.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.IService;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.Topic;


@Service
public interface TopicService extends IService<Topic> {


    R<List<Topic>> getTopicList();
    


}
