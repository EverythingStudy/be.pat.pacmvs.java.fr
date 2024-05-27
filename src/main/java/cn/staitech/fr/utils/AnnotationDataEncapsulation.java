package cn.staitech.fr.utils;

import cn.staitech.fr.vo.geojson.Features;
import cn.staitech.fr.vo.geojson.PropertiesBriefly;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class AnnotationDataEncapsulation {

    public static Features socketData(String annotationId, JSONObject geometry, PropertiesBriefly properties) {
        Features features = new Features();
        features.setGeometry(geometry);
        features.setId(annotationId);
        features.setType("Feature");
        String s1 = JSONObject.toJSONString(properties, SerializerFeature.PrettyFormat);
        JSONObject jsonObject = JSONObject.parseObject(s1);
        features.setProperties(jsonObject);
        return features;
    }

}
