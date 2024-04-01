package cn.staitech.fr.service;

import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.vo.annotation.AnnotationById;
import cn.staitech.fr.vo.annotation.AnnotationSelectList;
import cn.staitech.fr.vo.annotation.MarkingMerge;
import cn.staitech.fr.vo.geojson.Features;
import cn.staitech.fr.vo.geojson.in.UpdateOperationIn;
import cn.staitech.fr.vo.geojson.in.ViewAddIn;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author admin
* @description 针对表【fr_annotation】的数据库操作Service
* @createDate 2024-04-01 09:42:42
*/
public interface AnnotationService extends IService<Annotation> {

    List<Features> selectListBy(AnnotationSelectList annotation) throws Exception;

    Long insert(ViewAddIn req) throws Exception;

    void delete(AnnotationById req) throws Exception;

    Long update(ViewAddIn marking) throws Exception;



    JSONObject updateOperation(UpdateOperationIn req, String traceId, Boolean isBatch) throws Exception;

}
