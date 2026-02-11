package cn.staitech.fr.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.staitech.fr.domain.FrAnnotation;
import cn.staitech.fr.mapper.FrAnnotationMapper;
import cn.staitech.fr.service.FrAnnotationService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author adminimport static cn.staitech.fr.utils.AnnotationDataEncapsulation.socketData
 * @description 针对表【fr_annotation】的数据库操作Service实现
 * @createDate 2024-04-01 09:42:42
 */
@Service
@Slf4j
public class FrAnnotationServiceImpl extends ServiceImpl<FrAnnotationMapper, FrAnnotation> implements FrAnnotationService {
	@Resource
    private FrAnnotationMapper frAnnotationMapper;

    /**
     * 批量保存
     *
     * @param annotation
     * @param batchSize
     */
    @Override
    public void batchProcessAndSave(List<FrAnnotation> annotation) {
    	frAnnotationMapper.batchSave(annotation);

    }

}




