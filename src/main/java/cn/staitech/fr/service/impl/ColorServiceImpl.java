package cn.staitech.fr.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.staitech.fr.domain.Color;
import cn.staitech.fr.service.ColorService;
import cn.staitech.fr.mapper.ColorMapper;
import org.springframework.stereotype.Service;

/**
* @author 86186
* @description 针对表【tb_color】的数据库操作Service实现
* @createDate 2025-06-03 13:18:20
*/
@Service
public class ColorServiceImpl extends ServiceImpl<ColorMapper, Color>
    implements ColorService{

}




