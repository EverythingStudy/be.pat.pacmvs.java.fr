package cn.staitech.fr.service;

import cn.staitech.fr.domain.Annotation;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author admin
 * @description 针对表【fr_annotation】的数据库操作Service
 * @createDate 2024-04-01 09:42:42
 */
public interface AnnotationService extends IService<Annotation> {
    /**
     * 批量保存
     *
     * @param annotation
     * @param batchSize
     */
    void batchProcessAndSave(Annotation annotation, int batchSize);

}
