package cn.staitech.fr.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKTReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * GeometryUtil工具类
 *
 * @author admin
 */
public class GeometryUtil {

    /**
     * 初始化WKTReader
     *
     * @return
     */
    public static WKTReader initWktReader() {

        // GeometryFactory工厂，参数一：数据精度 参数二空间参考系SAID
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
        // 熟知文本WKT阅读器，可以将WKT文本转换为Geometry对象
        WKTReader wktReader = new WKTReader(geometryFactory);

        return wktReader;
    }


    public static JSONObject updateYAxle(JSONObject geometry) {
        List<Object> lists = new ArrayList<>();
        JSONArray coordinatesJsonArray1 = geometry.getJSONArray("coordinates");
        String type = geometry.getString("type");
        if (Objects.equals(type, "Polygon")) {
            for (Object i1 : coordinatesJsonArray1) {
                List<Object> list1 = new ArrayList<>();
                JSONArray jsonArray1 = JSONArray.parseArray(i1.toString());
                for (Object i2 : jsonArray1) {
                    JSONArray jsonArray2 = JSONArray.parseArray(i2.toString());
                    List<Double> list = JSONObject.parseArray(jsonArray2.toJSONString(), Double.class);
                    List<Double> newList = new ArrayList<>();
                    newList.add(list.get(0));
                    newList.add(opposite(list.get(1)));
                    list1.add(newList);
                }
                lists.add(list1);
            }
        } else if (Objects.equals(type, "LineString")) {
            for (Object i1 : coordinatesJsonArray1) {
                JSONArray jsonArray2 = JSONArray.parseArray(i1.toString());
                List<Double> list = JSONObject.parseArray(jsonArray2.toJSONString(), Double.class);
                List<Double> newList = new ArrayList<>();
                newList.add(list.get(0));
                newList.add(opposite(list.get(1)));
                lists.add(newList);
            }
        } else if (Objects.equals(type, "Point")) {
            List<Double> list = JSONObject.parseArray(coordinatesJsonArray1.toJSONString(), Double.class);
            lists.add(list.get(0));
            lists.add(opposite(list.get(1)));
        }
        JSONObject geometryJson = new JSONObject();
        geometryJson.put("type", type);
        geometryJson.put("coordinates", lists);
        return geometryJson;
    }


    public static Double opposite(Double y) {
        if (y == 0) {
            return y;
        } else if (y > 0) {
            return -y;
        } else {
            return Double.valueOf(String.valueOf(Math.abs(y)));
        }
    }
}
