package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.SingleOrganNumber;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.in.OrganDisassemblyQueryIn;
import cn.staitech.fr.domain.out.OrganDisassemblyOut;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * @author admin
 * @description 针对表【fr_slide(专题选片表)】的数据库操作Mapper
 * @createDate 2024-03-29 13:33:37
 * @Entity cn.staitech.fr.domain.Slide
 */
public interface SingleSlideMapper extends BaseMapper<SingleSlide> {
    List<OrganDisassemblyOut> selectSingleOrgan(OrganDisassemblyQueryIn req);

    @DS("slave")
    List<SingleOrganNumber> selectNumber(@Param("slideIds") List<Long> slideIds, @Param("categoryId") Long categoryId);

}




