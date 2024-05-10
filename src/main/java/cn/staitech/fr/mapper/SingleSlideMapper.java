package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.SingleOrganNumber;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.OrganDisassemblyQueryIn;
import cn.staitech.fr.domain.in.SingleSlideAdjacent;
import cn.staitech.fr.domain.out.ExportAiVO;
import cn.staitech.fr.domain.out.ExportVO;
import cn.staitech.fr.domain.out.OrganDisassemblyOut;
import cn.staitech.fr.domain.out.SingleSlideSelectBy;
import cn.staitech.fr.domain.out.SlideSelectBy;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface SingleSlideMapper extends BaseMapper<SingleSlide> {
    List<OrganDisassemblyOut> selectSingleOrgan(OrganDisassemblyQueryIn req);

    @DS("slave")
    List<SingleOrganNumber> selectNumber(@Param("slideIds") List<Long> slideIds, @Param("categoryId") Long categoryId);

    ExportVO getExportVO(Long id);

    ExportAiVO getExportAiVO(Long id);

    SingleSlideSelectBy singleSlideBy(Long singleId);

    List<SingleSlideSelectBy> singleSlideList(SingleSlideAdjacent singleSlideAdjacent);
}




