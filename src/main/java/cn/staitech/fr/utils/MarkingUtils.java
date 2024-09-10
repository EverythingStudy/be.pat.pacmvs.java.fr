package cn.staitech.fr.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKTReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.geotools.geojson.GeoJSONUtil;
import org.geotools.geojson.geom.GeometryJSON;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.staitech.fr.utils.DateUtils.MillisDefaultZone;

@Slf4j
public class MarkingUtils {
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    private static final WKTReader WKT_READER = new WKTReader(GEOMETRY_FACTORY);

    private static final String GEO_JSON_TYPE = "GeometryCollection";


    private static final String AI = "ai";
    private static final String SD = "sd";
    private static final String CL = "cl";
    private static final String LABEL_NAME = "labelname";
    private static final String MEASURE_NAME = "measure_name";

    public static int RandomNumbers() {
        return (int) (Math.random() * 90 + 10);
    }

    public static String getAiId() {
        return AI + LABEL_NAME + MillisDefaultZone() + RandomNumbers();
    }

    public static String getSdId() {
        return SD + LABEL_NAME + MillisDefaultZone() + RandomNumbers();
    }

    public static String getSdId(String categoryName) {
        return SD + categoryName + MillisDefaultZone() + RandomNumbers();
    }

    public static String getClId() {
        return CL + MEASURE_NAME + MillisDefaultZone() + RandomNumbers();
    }


    public static Geometry addVerify(JSONObject location) throws Exception {
        Geometry geometry;
        try {
            String oldLocation = WktUtil.jsonToWkt(location);
            // 使用WKT将字符串location转换为geometry对象
            geometry = WKT_READER.read(oldLocation);
            // 获取geometry对象类型
            String geometryType = geometry.getGeometryType();
            // 判断location是否为混合类型
            if (!"GeometryCollection".equals(geometryType)) {
                // 判断是否为复杂多边形
                geometry.union(geometry);
            }
//            boolean isClosed();
//            if (geometry.is) {
//                // 判断是否为复杂多边形
//                geometry.union(geometry);
//            }
            // 判断新增图形是否为多聚体
            if ("MultiPolygon".equals(geometryType)) {
                throw new Exception(MessageSource.M("GRAPHICS_MARK_NOT_RULES"));
            }
        } catch (Exception e) {
            throw new Exception(MessageSource.M("GRAPHICS_MARK_NOT_RULES"));
        }
        return geometry;
    }


    /**
     * @param oldLocations
     * @param newLocations
     * @param operation
     * @param check        true 校验，false校验
     * @return
     * @throws Exception
     */
    public static Boolean updateVerify(JSONObject oldLocations, JSONObject newLocations, String operation, boolean check, String resolution) {
        try {
            String oldLocation = WktUtil.jsonToWkt(oldLocations);
            String newLocation = WktUtil.jsonToWkt(newLocations);
            // 校验是否有要执行的操作  相交或者相差
            if (StringUtils.isNotBlank(operation)) {
                Geometry geometry1;
                Geometry geometry2;
                try {
                    geometry1 = WKT_READER.read(oldLocation);
                    geometry2 = WKT_READER.read(newLocation);
                } catch (Exception e) {
                    return false;
                }
                if (check) {
                    try {
                        // 新图形自相交
                        if (!geometry2.isSimple()) {
                            return false;
                        }
                        String geometryType = geometry2.getGeometryType();
                        // 判断新增图形是否为Polygon
                        if (!"Polygon".equals(geometryType)) {
                            return false;
                        }
                        Geometry geometryIntersection = geometry1.intersection(geometry2);
                        // 判断图形是否有交集 数据为空,两图形之间没有交集
                        if (geometryIntersection.isEmpty()) {
                            return false;
                        }
                    } catch (Exception e) {
                        return false;
                    }
                }
                if (check) {
                    if (geometry1.within(geometry2)) {
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {

            return false;
        }
    }



    /**
     * 剔除不规则点：
     *
     * @param geometry
     * @return
     */
    public static JSONObject padding(JSONObject geometry) throws Exception {
        JSONObject geometryJson = new JSONObject();
        try {
            JSONArray coordinatesJsonArray1 = geometry.getJSONArray("coordinates");
            String type = geometry.getString("type");
            if (Objects.equals(type, "Polygon")) {
                List<Object> list = new ArrayList<>();
                list.add(coordinatesJsonArray1.get(0));
                geometryJson.put("type", type);
                geometryJson.put("coordinates", list);
            }
        } catch (Exception e) {
            throw new Exception(MessageSource.M("GRAPHICS_MARK_NOT_RULES"));
        }
        return geometryJson;
    }




    public static String jsonToWkt(JSONObject jsonObject) {
        String wkt = null;
        String type = jsonObject.getString("type");
        GeometryJSON gJson = new GeometryJSON();
        try {
            // {"geometries":[{"coordinates":[4,6],"type":"Point"},{"coordinates":[[4,6],[7,10]],"type":"LineString"}],"type":"GeometryCollection"}
            if (GEO_JSON_TYPE.equals(type)) {
                // 由于解析上面的json语句会出现这个geometries属性没有采用以下办法
                JSONArray geometriesArray = jsonObject.getJSONArray("geometries");
                // 定义一个数组装图形对象
                int size = geometriesArray.size();
                Geometry[] geometries = new Geometry[size];
                for (int i = 0; i < size; i++) {
                    String str = geometriesArray.get(i).toString();
                    // 使用GeoUtil去读取str
                    Reader reader = GeoJSONUtil.toReader(str);
                    Geometry geometry = gJson.read(reader);
                    geometries[i] = geometry;
                }
                GeometryCollection geometryCollection = new GeometryCollection(geometries, new GeometryFactory());
                wkt = geometryCollection.toText();
            } else {
                Reader reader = GeoJSONUtil.toReader(jsonObject.toString());
                Geometry read = gJson.read(reader);
                wkt = read.toText();
            }

        } catch (IOException e) {
            System.out.println("GeoJson转WKT出现异常");
            e.printStackTrace();
        }
        return wkt;
    }


}
