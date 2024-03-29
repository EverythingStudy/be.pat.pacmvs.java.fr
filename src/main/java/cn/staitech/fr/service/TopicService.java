package cn.staitech.fr.service;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.Topic;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public interface TopicService extends IService<Topic> {


    R<List<Topic>> getTopicList();
}
