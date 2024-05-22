package cn.staitech.fr.service.impl;

import java.util.List;

import javax.annotation.Resource;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.common.security.utils.SecurityUtils;
import cn.staitech.fr.domain.in.ChoiceImageListInVo;
import cn.staitech.fr.domain.out.ImageListOutVO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.staitech.fr.domain.Image;
import cn.staitech.fr.mapper.ImageMapper;
import cn.staitech.fr.service.ImageService;
import lombok.extern.slf4j.Slf4j;

import static cn.staitech.common.security.utils.SecurityUtils.isAdmin;

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
    public boolean exists(Image image) {
        LambdaQueryWrapper<Image> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Image::getImageId);
        queryWrapper.eq(Image::getMd5, image.getMd5());
        queryWrapper.eq(Image::getOrganizationId, image.getOrganizationId());
        queryWrapper.orderByDesc(Image::getImageId);
        queryWrapper.last("limit 1");
        return this.baseMapper.selectOne(queryWrapper) != null;
    }
    @Override
    public PageResponse<ImageListOutVO> choiceImageList(ChoiceImageListInVo image) {
        log.info("评审选片列表分页查询接口开始：");
        if(!isAdmin(SecurityUtils.getUserId())){
            image.setOrgId(SecurityUtils.getLoginUser().getSysUser().getOrganizationId());
        }
        PageResponse<ImageListOutVO> pageResponse = new PageResponse<>();
        Page<ImageListOutVO> page = PageHelper.startPage(image.getPageNum(), image.getPageSize());

        List<ImageListOutVO> respData=this.baseMapper.choiceImageList(image);
        pageResponse.setTotal(page.getTotal());
        pageResponse.setList(respData);
        pageResponse.setPages(page.getPages());
        return pageResponse;
    }
}
