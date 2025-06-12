package cn.staitech.fr.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.staitech.fr.domain.UserSetting;
import cn.staitech.fr.service.UserSettingService;
import cn.staitech.fr.mapper.UserSettingMapper;
import org.springframework.stereotype.Service;

/**
 * @author 86186
 * @version 2.6.0
 * @description 针对表【tb_user_setting(用户设置表)】的数据库操作Service实现
 * @createDate 2025-05-15 17:25:39
 */
@Service
public class UserSettingServiceImpl extends ServiceImpl<UserSettingMapper, UserSetting>
        implements UserSettingService {

}




