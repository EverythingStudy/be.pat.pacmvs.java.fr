package cn.staitech.fr.service;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.Slide;
import cn.staitech.fr.domain.SpecialAnnotationRel;
import cn.staitech.fr.vo.annotation.Features;
import cn.staitech.fr.vo.annotation.in.*;
import cn.staitech.fr.vo.annotation.out.AnnotationDistanceOut;
import cn.staitech.fr.vo.history.HistoryDTO;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author admin
* @description 针对表【fr_contour】的数据库操作Service
* @createDate 2024-09-10 09:31:06
*/
public interface AnnotationService extends IService<Annotation> {

    List<Features> selectListBy(AnnotationSelectList annotation) throws Exception;
//
//    List<Features> aiSelectListBy(AnnotationSelectList annotation) throws Exception;
//
    Long insert(ViewAddIn req) throws Exception;

    public  Long getSequenceNumber(Long slideId);


    int delete(AnnotationById req) throws Exception;
//
    R<String> roiContDel(RoiIn viewAddIns) throws Exception;
//
//
    Long update(ViewAddIn marking) throws Exception;
    int padding(AnnotationById req) throws Exception;
    int stickup(AnnotationById req) throws Exception;
    JSONObject markingMerge(MarkingMerge req) throws Exception;
//
    JSONObject updateOperation(UpdateOperationIn req, String traceId, Boolean isBatch) throws Exception;
//
    List<BatchResult> batch(ViewAddInList list) throws Exception;
//
    Boolean undo(HistoryDTO dto);

    Boolean redo(HistoryDTO dto);

    Annotation deleteByHistory(Long annotationId,Long seq) throws Exception;

    Annotation insertByHistory(Annotation annotation);

    Annotation updateOperationByHistory(Annotation annotation);
//
//    /**
//     * 批量保存
//     * @param annotation
//     * @param batchSize
//     */
//    void batchProcessAndSave(Annotation annotation, int batchSize);

    AnnotationDistanceOut getDistance(DistanceGet distanceGet);
    
    boolean getCountByCategory(CategoryStatisticsIn req);

}
