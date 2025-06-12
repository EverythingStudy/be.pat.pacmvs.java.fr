package cn.staitech.fr.service;

import cn.staitech.common.core.domain.CustomPage;
import cn.staitech.fr.domain.Image;
import cn.staitech.fr.vo.image.ImageStatusVo;
import cn.staitech.fr.vo.image.ImageBatchIdsVO;
import cn.staitech.fr.vo.image.ImagePageReq;
import cn.staitech.fr.vo.image.ImageUpdateVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.text.ParseException;
import java.util.List;

/**
 * @author 94024
 * @description 针对表【tb_image】的数据库操作Service
 * @createDate 2024-09-10 10:21:48
 */
public interface ImageService extends IService<Image> {

    List<ImageStatusVo> status();

    CustomPage<Image> pageImage(ImagePageReq findIn) throws ParseException;

    List<Long> deleteBatchIds(ImageBatchIdsVO ids);

    int updateById(ImageUpdateVO vo);
}
