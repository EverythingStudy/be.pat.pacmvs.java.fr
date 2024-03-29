package cn.staitech.fr.service.impl;

import cn.staitech.common.core.domain.R;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.domain.Topic;
import cn.staitech.fr.mapper.TopicMapper;
import cn.staitech.fr.service.TopicService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {

    @Override
    public R<List<Topic>> getTopicList() {
        log.info("获取专题列表接口开始：");
        Long organizationId = SecurityUtils.getLoginUser().getSysUser().getOrganizationId();
        List<Topic> list = this.baseMapper.getTopicList(organizationId);
        return R.ok(list);
    }


}
