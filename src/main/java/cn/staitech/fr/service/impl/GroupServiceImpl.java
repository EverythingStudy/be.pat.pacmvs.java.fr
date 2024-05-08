package cn.staitech.fr.service.impl;

import cn.staitech.fr.domain.Group;
import cn.staitech.fr.mapper.GroupMapper;
import cn.staitech.fr.service.GroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;



/**
 * 分组 服务层实现
 *
 * @author
 */
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService {

}
