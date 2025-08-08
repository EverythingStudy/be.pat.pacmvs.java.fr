package cn.staitech.fr.mapper;

import cn.staitech.common.core.domain.CustomPage;
import cn.staitech.fr.vo.project.ChoiceImagePageReq;
import cn.staitech.fr.vo.project.ImageVO;
import cn.staitech.fr.vo.image.ImagePageReq;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import cn.staitech.fr.domain.Image;
import org.apache.ibatis.annotations.Param;

/**
 * 切片表（原图像表）数据层
 *
 * @author staitech
 */
public interface ImageMapper extends BaseMapper<Image> {
    CustomPage<Image> pageImage(CustomPage page, @Param("params") ImagePageReq params);

    CustomPage<ImageVO> choiceImageList(CustomPage page, @Param("params") ChoiceImagePageReq image);
    /**
     * 查询单个切片信息
     *
     * @param imageId
     * @return
     */
    Image selectById(Long imageId);
}