package cn.staitech.fr.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.staitech.fr.domain.Image;
import cn.staitech.fr.service.ImageService;
import cn.staitech.fr.mapper.ImageMapper;
import org.springframework.stereotype.Service;

/**
* @author admin
* @description 针对表【tb_image】的数据库操作Service实现
* @createDate 2024-09-10 10:44:42
*/
@Service
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image>
    implements ImageService{

}




