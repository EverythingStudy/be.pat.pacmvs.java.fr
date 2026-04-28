package cn.staitech.fr.mapper;


import java.util.List;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import cn.staitech.fr.domain.FrAnnotation;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wanglibei
 * @since 2026-02-11
 */
@DS("slave")
public interface FrAnnotationMapper extends BaseMapper<FrAnnotation> {
	void batchSave(List<FrAnnotation> annotation);
}
