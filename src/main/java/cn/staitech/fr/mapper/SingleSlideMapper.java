package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.SingleOrganNumber;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.dto.ExportAiDTO;
import cn.staitech.fr.domain.in.OrganDisassemblyQueryIn;
import cn.staitech.fr.domain.out.ExprotAiExcelVO;
import cn.staitech.fr.domain.out.OrganDisassemblyOut;
import cn.staitech.fr.domain.out.SingleSlideSelectBy;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;


public interface SingleSlideMapper extends BaseMapper<SingleSlide> {

    String getRangOut(@Param("quantitativeIndicators") String quantitativeIndicators, @Param("categoryId") Long categoryId, @Param("specialId") Long specialId, @Param("groupCode") String groupCode);

    String getImageId(Long slideId);

    List<BigDecimal> getReferenceScope(@Param("quantitativeIndicators") String quantitativeIndicators, @Param("categoryId") Long aLong, @Param("specialId") Long specialId, @Param("groupCode") String groupCode, @Param("genderFlag") String genderFlag, @Param("structType") String structType);

    List<BigDecimal> getReferenceScopeCopy(@Param("quantitativeIndicators") String quantitativeIndicators,
                                           @Param("categoryId") Long categoryId,
                                           @Param("specialId") Long specialId,
                                           @Param("groupCode") String groupCode);

    String getGender(Long id);

    SingleSlideSelectBy singleSlideBy(Long singleId);

    List<OrganDisassemblyOut> selectSingleOrgan(OrganDisassemblyQueryIn req);

    @DS("slave")
    List<SingleOrganNumber> selectNumber(@Param("slideIds") List<Long> slideIds, @Param("categoryId") Long categoryId);

    ExprotAiExcelVO getExportAiVO(Long id);

    ExportAiDTO getExportSingleSlideInfoById(Long id);

    Integer getCategoryIdCountByGroupCode(@Param("categoryId") Long categoryId,
                                          @Param("specialId") Long specialId,
                                          @Param("groupCode") String groupCode);
}




