package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.domain.in.MatrixReviewListIn;
import cn.staitech.fr.domain.in.SlideListQueryIn;
import cn.staitech.fr.domain.in.SplitVerificationQueryIn;
import cn.staitech.fr.domain.out.MatrixReviewListOut;
import cn.staitech.fr.domain.out.SlideListQueryOut;
import cn.staitech.fr.domain.out.SlideSelectBy;
import cn.staitech.fr.domain.out.SplitVerificationOut;
import cn.staitech.fr.domain.out.AlgorithmImageOut;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;


/**
* @author admin
* @description 针对表【fr_slide(专题选片表)】的数据库操作Mapper
* @createDate 2024-03-29 13:33:37
* @Entity cn.staitech.fr.domain.Slide
*/
public interface SlideMapper extends BaseMapper<Slide> {
    String selectBySpecialId(Long specialId);
    /**
     * 
    * @Title: getAlgorithmImage
    * @Description: 查下启动的所有切片信息
    * @param @param queryMap
    * @param @return
    * @return List<AlgorithmImageOut>
    * @throws
     */
    List<AlgorithmImageOut> getAlgorithmImage(Map<String,Object> queryMap);

    List<SlideListQueryOut> slideListQuery(SlideListQueryIn req);

    List<Slide> selectListByWax(@Param("topicId") Long topicId, @Param("speciesId")String speciesId);
    
    List<SplitVerificationOut> getVerificationSlideListQuery(SplitVerificationQueryIn req);

    SlideSelectBy pageImageCsvListVOBy(Long slideId);

    List<MatrixReviewListOut> getMatrixReview(MatrixReviewListIn req);

}




