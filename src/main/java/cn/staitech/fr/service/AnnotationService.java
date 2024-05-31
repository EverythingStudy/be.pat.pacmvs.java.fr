package cn.staitech.fr.service;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.history.HistoryDTO;
import cn.staitech.fr.vo.annotation.AnnotationById;
import cn.staitech.fr.vo.annotation.AnnotationCountByCategory;
import cn.staitech.fr.vo.annotation.AnnotationSelectList;
import cn.staitech.fr.vo.annotation.MarkingMerge;
import cn.staitech.fr.vo.geojson.Features;
import cn.staitech.fr.vo.geojson.in.DistanceGet;
import cn.staitech.fr.vo.geojson.in.RoiIn;
import cn.staitech.fr.vo.geojson.in.UpdateOperationIn;
import cn.staitech.fr.vo.geojson.in.ViewAddIn;
import cn.staitech.fr.vo.geojson.out.BatchResult;
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

    List<Features> aiSelectListBy(AnnotationSelectList annotation) throws Exception;

    Long insert(ViewAddIn req) throws Exception;

    int delete(AnnotationById req) throws Exception;

    R<String> roiContDel(RoiIn viewAddIns) throws Exception;


    Long update(ViewAddIn marking) throws Exception;
    int padding(AnnotationById req) throws Exception;
    int stickup(AnnotationById req) throws Exception;
    JSONObject markingMerge(MarkingMerge req) throws Exception;

    JSONObject updateOperation(UpdateOperationIn req, String traceId, Boolean isBatch) throws Exception;

    List<BatchResult> batch(List<ViewAddIn> list) throws Exception;
    
    Boolean undo(HistoryDTO dto);

    Boolean redo(HistoryDTO dto);

    Annotation deleteByHistory(Long annotationId) throws Exception;

    Annotation insertByHistory(Annotation annotation);

    Annotation updateOperationByHistory(Annotation annotation);

    /**
     * 批量保存
     * @param annotation
     * @param batchSize
     */
    void batchProcessAndSave(Annotation annotation, int batchSize);

    Annotation getDistance(DistanceGet distanceGet);
}
