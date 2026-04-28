package cn.staitech.fr.utils;

import cn.staitech.fr.utils.geo.GeoJson;
import cn.staitech.fr.utils.geo.MultiPolygonParser;
import cn.staitech.fr.utils.geo.PolygonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Geometry;

/**
 * GeometryUtil工具类
 *
 * @author admin
 */
@Slf4j
public class GeometryUtil {

    public static Boolean isValid(String geoJson) {
        boolean flag = false;
        try {
            if (StringUtils.isNotEmpty(geoJson)) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(geoJson);
                flag = isValid(jsonNode);
            }
        } catch (JsonProcessingException e) {
            log.error("Invalid JSON: [{}]----[{}]" , e.getMessage(), e);
        }
        return flag;
    }

    public static Boolean isValid(JsonNode jsonNode) {
        boolean flag = false;
        Geometry geometry = geometryFromJson(jsonNode);
        if (geometry != null){
            flag = geometry.isValid();
        }
        return flag;
    }

    public static Geometry geometryFromJson(String geoJson) {
        Geometry geometry = null;
        try {
            if (StringUtils.isNotEmpty(geoJson)) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(geoJson);
                geometry = geometryFromJson(jsonNode);
            }
        } catch (JsonProcessingException e) {
            log.error("Invalid JSON: [{}]----[{}]" , e.getMessage(), e);
        }
        return geometry;
    }

    public static Geometry geometryFromJson(JsonNode jsonNode) {
        Geometry geometry = null;
        try {
            JsonNode type = jsonNode.get(GeoJson.TYPE);
            if (GeoJson.MULTI_POLYGON.equals(type.asText())){
                geometry = MultiPolygonParser.geometryFromJson(jsonNode);
            }else if(GeoJson.POLYGON.equals(type.asText())){
                geometry = PolygonParser.geometryFromJson(jsonNode);
            }
        } catch (JsonMappingException e) {
            log.error("Error occurred while parsing JSON: [{}]----[{}]" , e.getMessage(), e);
        }
        return geometry;
    }


}
