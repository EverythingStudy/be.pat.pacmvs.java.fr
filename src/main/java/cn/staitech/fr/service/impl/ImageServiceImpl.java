package cn.staitech.fr.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.staitech.fr.domain.Image;
import cn.staitech.fr.mapper.ImageMapper;
import cn.staitech.fr.service.ImageService;
import lombok.extern.slf4j.Slf4j;

/**
 * 切片列表（原图像）服务层实现
 *
 * @author wangfeng
 * @date 2023/12/20
 */
@Slf4j
@Service
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image> implements ImageService {
    @Resource
    private ImageMapper imageMapper;


    /**
     * 查询单个图像信息
     *
     * @param image
     * @return
     */
    @Override
    public Image selectById(Long image) {
        return imageMapper.selectById(image);
    }

    /**
     * 通过图片ID查询切片
     *
     * @param imageId
     * @return
     */
    @Override
    public Integer selectSlideCountByImageId(Long imageId) {
        return imageMapper.selectSlideCountByImageId(imageId);
    }


    /**
     * 标注组图像列表
     *
     * @param image
     * @return
     */
    @Override
    public List<Image> selectImageAnnotationList(Image image) {
        return imageMapper.selectImageAnnotationList(image);
    }



    /**
     * 检查是否存在符合条件的记录
     *
     * @param image
     * @return
     */
    @Override
    public boolean exists(Image image) throws Exception {
        LambdaQueryWrapper<Image> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Image::getImageId);
        queryWrapper.eq(Image::getMd5, image.getMd5());
        queryWrapper.eq(Image::getOrganizationId, image.getOrganizationId());
        queryWrapper.orderByDesc(Image::getImageId);
        queryWrapper.last("limit 1");
        return this.baseMapper.selectOne(queryWrapper) != null;
    }

}
