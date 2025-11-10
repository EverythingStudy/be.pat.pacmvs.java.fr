package cn.staitech.fr.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.service.AnnotationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author adminimport static cn.staitech.fr.utils.AnnotationDataEncapsulation.socketData
 * @description 针对表【fr_annotation】的数据库操作Service实现
 * @createDate 2024-04-01 09:42:42
 */
@Service
@Slf4j
public class AnnotationServiceImpl extends ServiceImpl<AnnotationMapper, Annotation> implements AnnotationService {

    @Resource
    private AnnotationMapper annotationMapper;

    /**
     * 批量保存
     *
     * @param annotation
     * @param batchSize
     */
    @Override
    public void batchProcessAndSave(Annotation annotation, int batchSize) {

        List<Annotation> annotations = annotation.getList();
        if (CollectionUtil.isEmpty(annotations)) {
            return;
        }
        int listSize = annotations.size();

        // 分批处理
        for (int i = 0; i < listSize; i += batchSize) {
            int endIndex = Math.min(i + batchSize, listSize);
            List<Annotation> batch = annotations.subList(i, endIndex);
            Annotation annotation1 = new Annotation();
            annotation1.setSequenceNumber(annotation.getSequenceNumber());
            annotation1.setList(batch);
            try {
                log.info("开始批量保存");
                annotationMapper.batchSave(annotation1);
            } catch (Exception e) {
                log.info("开始批量保存异常");
                // 处理异常，例如记录日志
                List<Annotation> annotationList = new ArrayList<>();
                for (Annotation annotationBy : batch) {
                    try {
                        annotationMapper.stIsValidAnnotation(annotationBy);
                        annotationList.add(annotationBy);
                    } catch (Exception a) {
                        log.error("Error occurred while processing batch: " + e.getMessage(), e);
                    }
                }
                annotation1.setList(annotationList);
                try {
                    annotationMapper.batchSave(annotation1);
                } catch (Exception a) {
                    log.error("Error occurred while processing batch: " + e.getMessage(), e);
                }
            }
        }
    }

}




