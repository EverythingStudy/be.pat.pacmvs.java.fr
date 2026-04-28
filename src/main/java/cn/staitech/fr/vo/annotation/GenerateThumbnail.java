package cn.staitech.fr.vo.annotation;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;

@Data
public class GenerateThumbnail {
    private Long slideId;
    private Long categoryId;
    private String svsPath;
    private List<JSONObject> slideRoiPolygon;
    private int types;
    private String abbreviation;
    private String topicName;
}
