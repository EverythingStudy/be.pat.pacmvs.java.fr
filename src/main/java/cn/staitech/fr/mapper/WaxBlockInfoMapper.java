package cn.staitech.fr.mapper;

import cn.staitech.fr.domain.WaxBlockInfo;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 蜡块编号明细 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2024-03-29
 */
public interface WaxBlockInfoMapper extends BaseMapper<WaxBlockInfo> {

    String getOrganName(@Param("topicId") Long topicId, @Param("speciesId") String speciesId,@Param("waxCode") String waxCode,@Param("genderFlag")String genderFlag);

	List<WaxBlockInfo> getWaxBlockInfoList(Map<String,Object> parm);
	
	List<WaxBlockInfo> getSpecialWaxBlockInfoList(Map<String,Object> parm);
}
