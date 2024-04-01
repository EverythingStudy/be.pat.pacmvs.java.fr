package cn.staitech.fr.mapper;

import java.util.List;

import cn.staitech.fr.domain.in.ChoiceImageListInVo;
import cn.staitech.fr.domain.out.ImageListOutVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import cn.staitech.fr.domain.Image;

/**
 * 切片表（原图像表）数据层
 *
 * @author staitech
 */
public interface ImageMapper extends BaseMapper<Image> {

    /**
     * 查询切片列表
     *
     * @param image
     * @return
     */
    List<Image> selectListSlfe(Image image);

    /**
     * 查询未添加切片列表
     *
     * @param image
     * @return
     */
    List<Image> selectNotChoicedList(Image image);

    /**
     * 查询已添加切片列表
     *
     * @param image
     * @return
     */
    List<Image> selectChoicedList(Image image);


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
     * 标注组选片入口预览图像列表
     *
     * @param image
     * @return
     */
    List<Image> selectImageChooseList(Image image);

    /**
     * 标注组图像列表
     *
     * @param image
     * @return
     */
    List<Image> selectImageAnnotationList(Image image);

    /**
     * 运维图像删除
     *
     * @param imageId
     * @return
     */
    int deleteById(Long imageId);

    List<ImageListOutVO> choiceImageList(ChoiceImageListInVo image);
}