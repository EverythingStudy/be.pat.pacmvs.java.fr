package cn.staitech.fr.service.impl;

import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.SpecialAnnotationRel;
import cn.staitech.fr.mapper.SpecialAnnotationRelMapper;
import cn.staitech.fr.vo.annotation.in.DistanceGet;
import cn.staitech.fr.vo.annotation.out.AnnotationDistanceOut;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.staitech.fr.service.AnnotationService;
import cn.staitech.fr.mapper.AnnotationMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
* @author admin
* @description 针对表【fr_contour】的数据库操作Service实现
* @createDate 2024-09-10 09:31:06
*/
@Service
public class AnnotationServiceImpl extends ServiceImpl<AnnotationMapper, Annotation>
    implements AnnotationService {

//    @Resource
//    private MeasureMapper measureMapper;

    @Resource
    private SpecialAnnotationRelMapper specialAnnotationRelMapper;
    @Resource
    private AnnotationMapper annotationMapper;

    @Override
    public AnnotationDistanceOut getDistance(DistanceGet req){
        AnnotationDistanceOut annotationDistanceOut = new AnnotationDistanceOut();
        LambdaQueryWrapper<SpecialAnnotationRel> queryWrapper = new LambdaQueryWrapper<SpecialAnnotationRel>().eq(SpecialAnnotationRel::getSpecialId, req.getSpecialId());
        SpecialAnnotationRel specialAnnotationRel = specialAnnotationRelMapper.selectOne(queryWrapper);
        String contourOne = selectContour(req.getAnnotationIdOne(),req.getAnnotationTypeOne(),req.getContourTypeOne(),specialAnnotationRel.getSequenceNumber());
        String contourTwo = selectContour(req.getAnnotationIdTwo(),req.getAnnotationTypeTwo(),req.getContourTypeTwo(),specialAnnotationRel.getSequenceNumber());
        Annotation annotation = new Annotation();
        annotation.setContourOne(contourOne);
        annotation.setContourTwo(contourTwo);
        // 计算两个图形之间最短距离的两个点
        Annotation annotationDistance = annotationMapper.stClosestPoint(annotation);
        annotationDistanceOut.setContourTypeOne(JSONObject.parseObject(annotationDistance.getContourOne()));
        annotationDistanceOut.setContourTypeTwo(JSONObject.parseObject(annotationDistance.getContourTwo()));
        // 计算两个图形之间的最短距离
        Annotation annotationMinDistance = annotationMapper.stDistance(annotation);
        Double minDistance = Double.parseDouble(String.format("%.3f", annotationMinDistance.getMinDistance()));
        annotationDistanceOut.setMinDistance(minDistance);
        // 计算两个图形之间的平均距离
        Annotation annotationAvgDistance = annotationMapper.avgDistance(annotation);
        Double meanDistance = Double.parseDouble(String.format("%.3f", annotationAvgDistance.getMeanDistance()));
        annotationDistanceOut.setMeanDistance(meanDistance);
        return annotationDistanceOut;
    }






    private String selectContour(Long annotationId,String annotationType,Long contourType,Long sequenceNumber) {
        String contour = null;
        if(Objects.equals(annotationType, "Draw")){
            // 根据主键查询fr_annotation表中详情信息
            contour = annotationMapper.selectByIds(annotationId).getContour();
        } else if (Objects.equals(annotationType, "Measure")) {
            // 根据主键查询fr_measure表中信息
//            contour = measureMapper.selectById(annotationId).getContour();
        }else{
            if(contourType == 3){
                // 根据主键查询fr_annotation表中详情信息
                contour = annotationMapper.selectByIds(annotationId).getContour();
            }else{
                // 查询fr_ai_annotation表中信息
                Annotation annotation = new Annotation();
                annotation.setAnnotationId(annotationId);
                annotation.setSequenceNumber(sequenceNumber);
//                contour = annotationMapper.aiSelectById(annotation).getContour();
            }
        }
        return contour;
    }

}




