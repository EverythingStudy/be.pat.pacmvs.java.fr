package cn.staitech.fr.utils;

import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.mapper.PathologicalIndicatorCategoryMapper;
import cn.staitech.fr.vo.geojson.Features;
import cn.staitech.fr.vo.geojson.Properties;
import cn.staitech.fr.vo.measure.BroadcastVO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import javax.annotation.Resource;
import java.util.ArrayList;

public class AnnotationDataEncapsulation {
    @Resource
    private PathologicalIndicatorCategoryMapper categoryMapper;


    public static Properties getProperties(Annotation annotation) {
        Properties properties = new Properties();

        properties.setArea(annotation.getArea());
        properties.setPerimeter(annotation.getPerimeter());
        properties.setAnnotation_type(annotation.getAnnotationType());
        properties.setCategory_id(annotation.getCategoryId());
        properties.setDescription(annotation.getDescription());
        properties.setLocation_type(annotation.getLocationType());
        properties.setCreate_time(String.valueOf(annotation.getCreateTime()));
        properties.setProject_id(annotation.getProjectId());
//        properties.setLabel_color();
        properties.setAnnotation_update_owner(String.valueOf(annotation.getUpdateBy()));
        properties.setAnnotation_owner(String.valueOf(annotation.getCreateBy()));
        return properties;
    }


    public static Features socketData(String annotationId, JSONObject geometry, Properties properties) {
        Features features = new Features();
        features.setGeometry(geometry);
        features.setId(annotationId);
        features.setType("Feature");
        JSONObject jsonObject = (JSONObject) JSON.toJSON(properties);
        features.setProperties(jsonObject);
        return features;
    }

    public static BroadcastVO sendOneMessages(String status, Features features) {
        BroadcastVO broadcast = new BroadcastVO();
        broadcast.setData(features);
        broadcast.setType(status);
        broadcast.setPoint_count_list(new ArrayList<>());
        return broadcast;
    }


}
