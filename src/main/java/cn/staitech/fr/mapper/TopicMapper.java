package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.Topic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;


public interface TopicMapper extends BaseMapper<Topic> {
    List<Topic> getTopicList(Long organizationId);
}

