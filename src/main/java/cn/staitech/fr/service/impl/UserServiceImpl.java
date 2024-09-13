package cn.staitech.fr.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.staitech.fr.domain.User;
import cn.staitech.fr.service.UserService;
import cn.staitech.fr.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author admin
* @description 针对表【sys_user(用户信息表)】的数据库操作Service实现
* @createDate 2024-09-10 10:43:07
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




