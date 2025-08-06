package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.SingleSlide;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;


public interface SingleSlideMapper extends BaseMapper<SingleSlide> {

    String getRangOut(@Param("quantitativeIndicators") String quantitativeIndicators, @Param("categoryId") Long categoryId, @Param("specialId") Long specialId, @Param("groupCode") String groupCode);

    String getImageId(Long slideId);

    List<BigDecimal> getReferenceScope(@Param("quantitativeIndicators") String quantitativeIndicators, @Param("categoryId") Long aLong, @Param("specialId") Long specialId, @Param("groupCode") String groupCode, @Param("genderFlag") String genderFlag, @Param("structType") String structType);

    String getGender(Long id);
}




