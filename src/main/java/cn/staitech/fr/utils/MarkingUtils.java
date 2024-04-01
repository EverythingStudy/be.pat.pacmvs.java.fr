package cn.staitech.fr.utils;

import cn.staitech.fr.domain.Measure;
import cn.staitech.fr.vo.geojson.Features;
import cn.staitech.fr.vo.geojson.Properties;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.operation.overlay.OverlayOp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.geotools.geojson.GeoJSONUtil;
import org.geotools.geojson.geom.GeometryJSON;


import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
     * 判断小轮廓防止失误裁剪
     *
     * @param oldLocations
     * @param newLocations
     * @param operation
     * @return
     * @throws Exception
     */
    public static double updateOperationVerify(JSONObject oldLocations, JSONObject newLocations, String operation) throws Exception {
        try {
            String oldLocation = WktUtil.jsonToWkt(oldLocations);

            String newLocation = WktUtil.jsonToWkt(newLocations);

            double percentage = 0;

            // 校验是否有要执行的操作  相交或者相差
            if (StringUtils.isNotBlank(operation)) {
                Geometry geometry1;
                try {
                    geometry1 = WKT_READER.read(oldLocation);
                } catch (Exception e) {
                    throw new Exception(MessageSource.M("GRAPHICS_MARK_NOT_RULES"));
                }
                Geometry geometry2;
                try {
                    geometry2 = WKT_READER.read(newLocation);
                } catch (Exception e) {
                    throw new Exception(MessageSource.M("GRAPHICS_MARK_NOT_RULES"));
                }
                // 新图形自相交
                if (!geometry2.isSimple()) {
                    System.out.println("新增图形自相交");
                    throw new Exception(MessageSource.M("GRAPHICS_MARK_NOT_RULES"));
                }
                String geometryType = geometry2.getGeometryType();
                // 判断新增图形是否为Polygon
                if (!"Polygon".equals(geometryType)) {
                    throw new Exception(MessageSource.M("GRAPHICS_MARK_NOT_RULES"));
                }

                // 校验旧图形在新图形中(新图形不能将旧图形完全覆盖)
                if (geometry1.within(geometry2)) {
                    throw new Exception(MessageSource.M("GRAPHICS_MARK_NOT_RULES"));
                }
                // 判断图形是否有效
//            if (geometry1.isValid() && geometry2.isValid()) {
                // 获取相交的geometry
                try {
                    Geometry geometryIntersection = geometry1.intersection(geometry2);
                    // 判断图形是否有交集 数据为空,两图形之间没有交集
                    if (geometryIntersection.isEmpty()) {
                        System.out.println("两图形之间没有交集");
                        throw new Exception(MessageSource.M("GRAPHICS_MARK_NOT_RULES"));
                    }
                } catch (Exception e) {
                    throw new Exception(MessageSource.M("GRAPHICS_MARK_NOT_RULES"));
                }

                // 使用新图形除以旧图形，获取新图形在旧图形中的占比
                percentage = geometry2.getArea() / geometry1.getArea();
            }
            return percentage;
        } catch (Exception e) {
            throw new Exception(MessageSource.M("GRAPHICS_MARK_NOT_RULES"));
        }

    }


    /**
     * @param oldLocations
     * @param newLocations
     * @param operation
     * @param check        true 校验，false校验
     * @return
     * @throws Exception
     */
    public static Measure updateVerify(JSONObject oldLocations, JSONObject newLocations, String operation, boolean check, String resolution) {
        Measure measure = new Measure();
        try {
            String oldLocation = WktUtil.jsonToWkt(oldLocations);
            String newLocation = WktUtil.jsonToWkt(newLocations);
            // WKT输出器，将Geometry对象写出为WKT文本
            WKTWriter wktWriter = new WKTWriter();
            String data = newLocation;
            // 校验是否有要执行的操作  相交或者相差
            if (StringUtils.isNotBlank(operation)) {
                Geometry geometry1;
                Geometry geometry2;
                try {
                    geometry1 = WKT_READER.read(oldLocation);
                    geometry2 = WKT_READER.read(newLocation);
                } catch (Exception e) {
                    measure.setException(MessageSource.M("GRAPHICS_MARK_NOT_RULES"));
                    return measure;
                }
                if (check) {
                    try {
                        // 新图形自相交
                        if (!geometry2.isSimple()) {
                            measure.setException(MessageSource.M("GRAPHICS_MARK_NOT_RULES"));
                            return measure;
                        }
                        String geometryType = geometry2.getGeometryType();
                        // 判断新增图形是否为Polygon
                        if (!"Polygon".equals(geometryType)) {
                            measure.setException(MessageSource.M("GRAPHICS_MARK_NOT_RULES"));
                            return measure;
                        }

                        Geometry geometryIntersection = geometry1.intersection(geometry2);
                        // 判断图形是否有交集 数据为空,两图形之间没有交集
                        if (geometryIntersection.isEmpty()) {
                            measure.setException(MessageSource.M("GRAPHICS_MARK_NOT_RULES"));
                            return measure;
                        }
                    } catch (Exception e) {
                        measure.setException(MessageSource.M("GRAPHICS_MARK_NOT_RULES"));
                        return measure;
                    }
                }
                OverlayOp op = new OverlayOp(geometry1, geometry2);
                int code = 0;
                // 取出交集图形
                if (check) {
                    if (geometry1.within(geometry2)) {
                        // 修改失败,请检查后输入
                        measure.setException(MessageSource.M("GRAPHICS_MARK_NOT_RULES"));
                        return measure;
                    }
                    // 校验标注不能过小，不能小于1000.0
//                        if (geometry2.within(geometry1) && geometry2.getArea() < insideMaxArea) {
//                            throw new AnnoException(AnnotationResponseConstant.UPDATE_ANNO_ERROR + geometry2.getArea());
//                        }
                }
                // 如果操作为相交
                if ("UNION".equals(operation)) {
                    code = OverlayOp.UNION;
                    // 操作为相差
                } else if ("DIFFERENCE".equals(operation)) {
                    code = OverlayOp.DIFFERENCE;
                }
                // 返回结果
                Geometry geometry;
//            try {
                // code=OverlayOp.UNION;相交  或者  code=OverlayOp.DIFFERENCE;相差
                // 将code转换成Geometry对象
                geometry = op.getResultGeometry(code);

                // 获取geometry类型
                String geometryType = geometry.getGeometryType();
                // TODO：校验飞点 后续可写多种校验策略放入线程池中
//                if ("Polygon".equals(geometryType)) {
//                    geometry = removePolygonPoint(geometry.getCoordinates());
//                }
                // 判断合并后图形是否为复杂多边型(比如大标注嵌套小标注)
                if (!"Polygon".equals(geometryType)) {
                    // throw new AnnoException(AnnotationResponseConstant.NEW_GRAPHICS_MARK_NOT_RULES);
                    // 新图形不符合规则
                    measure.setException(MessageSource.M("GRAPHICS_MARK_NOT_RULES"));
                    return measure;
                }
//            } catch (Exception e) {
//                throw new Exception(MessageSource.M("GRAPHICS_MARK_NOT_RULES"));
//            }
//            System.out.println(geometry + "//////////////////////");
//            if(geometry.isValid()){
//                throw new Exception(MessageSource.M("GRAPHICS_MARK_NOT_RULES"));
//            }
                data = wktWriter.write(geometry);
                measure.setData(data);
                if (resolution != null) {
                    double resolutions = Double.parseDouble(resolution);
                    String area = String.valueOf(geometry.getArea() * resolutions * resolutions);
                    measure.setArea(area);
                    String per = String.valueOf(geometry.getLength() * resolutions);
                    measure.setPerimeter(per);
                }

            }
            return measure;

        } catch (Exception e) {
            measure.setException(MessageSource.M("GRAPHICS_MARK_NOT_RULES"));
            return measure;
        }

    }

    /**
     * 剔除不规则点：
     *
     * @param geometry
     * @return
     */
    public static JSONObject updatePoint(JSONObject geometry) throws Exception {
        JSONObject geometryJson = new JSONObject();
        try {
            GeometryJSON gJson = new GeometryJSON();
            Reader reader = GeoJSONUtil.toReader(geometry.toString());
            Geometry read = gJson.read(reader);
            List<Double> xList = new ArrayList<>();
            List<Double> yList = new ArrayList<>();
            List<Object> lists = new ArrayList<>();
            JSONArray coordinatesJsonArray1 = geometry.getJSONArray("coordinates");
            String type = geometry.getString("type");
            if (Objects.equals(type, "Polygon")) {

                for (Object i1 : coordinatesJsonArray1) {
                    List<Object> list1 = new ArrayList<>();
                    JSONArray jsonArray1 = JSONArray.parseArray(i1.toString());
                    // 定义一个变量
                    Double x = null;
                    Double y = null;
                    for (Object i2 : jsonArray1) {
                        JSONArray jsonArray2 = JSONArray.parseArray(i2.toString());
                        List<Double> list = JSONObject.parseArray(jsonArray2.toJSONString(), Double.class);
                        List<Double> newList = new ArrayList<>();
                        double newX = 0;
                        double newY = 0;
                        if (x != null) {
                            // 取出绝对值
                            newX = Math.abs(x - list.get(0));
                        }
                        xList.add(list.get(0));
                        yList.add(list.get(1));
                        if (y != null) {
                            newY = Math.abs(y - list.get(1));
                        }
                        if (newX < 100 && newY < 100) {
                            newList.add(list.get(0));
                            newList.add(list.get(1));
                            list1.add(newList);
                        }
                        x = list.get(0);
                        y = list.get(1);
                    }
                    lists.add(list1);
                }
            }
            geometryJson.put("type", type);
            geometryJson.put("coordinates", lists);
        } catch (Exception e) {
            throw new Exception(MessageSource.M("GRAPHICS_MARK_NOT_RULES"));
        }
        return geometryJson;
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


    public static Geometry unionGeometrys(Geometry[] geos){
        GeometryFactory geometryFactory = new GeometryFactory();
        GeometryCollection geometryCollection = geometryFactory.createGeometryCollection(geos);
        return  geometryCollection.union();
//        return Geometry geometry;
    }

    /**
     * 剔除不规则点：剔除数值明显过大或过小的值，暂定踢除x,y中绝对值大于50000的点
     * 例如点存在科学记数法 (47713.255571202826, -6.989704038477149E14, NaN)
     *
     * @param coordinates Coordinate数组
     * @return
     */
    public static Geometry removePolygonPoint(Coordinate[] coordinates) {
        int length = coordinates.length;
        log.info("length: {} -------------------------", length);

        Coordinate[] tmpCoordinates = new Coordinate[length];
        int distLength = 0;
        for (int i = 0; i < coordinates.length; i++) {
/*
            if (Math.abs(coordinates[i].x) > 500000 || Math.abs(coordinates[i].y) > 500000) {
                log.info("{} {}", i, coordinates[i]);
                continue;
            }
*/
            double distince;
            if (i == length - 1) {
                distince = coordinates[i].distance(coordinates[0]);
            } else {
                distince = coordinates[i].distance(coordinates[i + 1]);
            }

            if (distince > 50000) {
                log.info("{} {} {} -------------------------", i, coordinates[i], distince);
                continue;
            }

            tmpCoordinates[distLength] = coordinates[i];
            distLength++;
        }

        Coordinate[] distCoordinates = new Coordinate[distLength];
        System.arraycopy(tmpCoordinates, 0, distCoordinates, 0, distLength);

        log.info("distCoordinates.length: {} -------------------------", distCoordinates.length);

        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
        return geometryFactory.createPolygon(distCoordinates);
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

    public static double precision(Double d) {
        BigDecimal bd = new BigDecimal(d);
        return bd.setScale(3, RoundingMode.DOWN).doubleValue();
    }


    public static JSONObject updatePrecision(JSONObject geometry) {
        List<Object> lists = new ArrayList<>();
        JSONArray coordinatesJsonArray1 = geometry.getJSONArray("coordinates");
        String type = geometry.getString("type");
        if (Objects.equals(type, "Polygon")) {
            List<Object> list1 = new ArrayList<>();
            for (Object i1 : coordinatesJsonArray1) {
                List<Object> list2 = new ArrayList<>();
                JSONArray jsonArray1 = JSONArray.parseArray(i1.toString());
                for (Object i2 : jsonArray1) {
                    JSONArray jsonArray2 = JSONArray.parseArray(i2.toString());
                    List<Double> list = JSONObject.parseArray(jsonArray2.toJSONString(), Double.class);
                    List<Double> newList = new ArrayList<>();
                    double newX = precision(list.get(0));
                    double newY = precision(list.get(1));
                    newList.add(newX);
                    newList.add(newY);
                    list2.add(newList);
                }
                list1.add(list2);
            }
            lists.add(list1);
        }
        JSONObject geometryJson = new JSONObject();
        geometryJson.put("type", type);
        geometryJson.put("coordinates", lists.get(0));
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
