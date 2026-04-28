package cn.staitech.fr.service;

import java.util.List;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.IService;

import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.FrAnnotation;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wanglibei
 * @since 2026-02-11
 */
@DS("slave")
public interface FrAnnotationService extends IService<FrAnnotation> {
	 void batchProcessAndSave(List<FrAnnotation> annotation);
}
