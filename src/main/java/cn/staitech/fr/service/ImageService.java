package cn.staitech.fr.service;

import java.util.List;

import cn.staitech.common.core.domain.PageResponse;
import cn.staitech.fr.domain.in.ChoiceImageListInVo;
import cn.staitech.fr.domain.out.ImageListOutVO;
import com.baomidou.mybatisplus.extension.service.IService;

import cn.staitech.fr.domain.Image;

/**
 * 图像 服务层
 *
 * @author wangfeng
 * @date 2023/06/01
 */
public interface ImageService extends IService<Image> {


    /**
     * 查询单个切片信息
     *
     * @param imageId
     * @return
     */
    Image selectById(Long imageId);

    /**
     * 通过图片ID查询切片
     *
     * @param imageId
     * @return
     */
    Integer selectSlideCountByImageId(Long imageId);


    /**
     * 标注组图像列表
     *
     * @param image
     * @return
     */
    List<Image> selectImageAnnotationList(Image image);


    /**
     * 检查是否存在否合条件的记录
     *
     * @param image
     * @return
     */
    boolean exists(Image image) throws Exception;

    PageResponse<ImageListOutVO> choiceImageList(ChoiceImageListInVo image);
}