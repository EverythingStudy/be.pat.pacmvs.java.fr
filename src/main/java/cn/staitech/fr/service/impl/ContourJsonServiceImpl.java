package cn.staitech.fr.service.impl;

import cn.staitech.common.core.domain.R;
import cn.staitech.fr.config.MapConstant;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.domain.out.ContourFileVo;
import cn.staitech.fr.domain.out.JsonFileVo;
import cn.staitech.fr.domain.out.SingleSlideSelectBy;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.ContourJsonMapper;
import cn.staitech.fr.mapper.ImageMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import cn.staitech.fr.service.ContourJsonService;
import cn.staitech.fr.service.strategy.json.CommonJsonParser;
import cn.staitech.fr.utils.AreaUtils;
import cn.staitech.fr.utils.GeometryUtil;
import cn.staitech.fr.vo.geojson.Features;
import cn.staitech.fr.vo.geojson.GeoJson;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.geotools.geojson.geom.GeometryJSON;
import org.json.JSONException;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.staitech.common.core.utils.DateUtils.parseDate;

/**
 * @author admin
 * @description 针对表【fr_contour_json】的数据库操作Service实现
 * @createDate 2024-10-15 14:58:30
 */
@Slf4j
@Service
public class ContourJsonServiceImpl extends ServiceImpl<ContourJsonMapper, ContourJson> implements ContourJsonService {

    @Resource
    private AnnotationMapper annotationMapper;

    @Resource
    private SingleSlideMapper singleSlideMapper;

    // json文件存储路径
    private final static String OUTPUT_FILTERED_FILE_PATH = File.separator + "home" + File.separator + "data" + File.separator + "aiJson";

    // 瓦片大小
    public static final int TILE_SIZE = 512;

    // 批量提交数量
    public static final int BATCH_SIZE = 4000;

    @Resource
    private CommonJsonParser commonJsonParser;


    ExecutorService EXECUTOR = new ThreadPoolExecutor((int) (Runtime.getRuntime().availableProcessors() * 3), Runtime.getRuntime().availableProcessors() * 6, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new CustomRejectedExecutionHandler());


    static class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            log.warn("Task {} is rejected because the queue is full", r.toString());
            // 可以在这里实现自定义的处理逻辑，例如记录日志、重试等
        }
    }


    @Override
    public void aiJson(List<JsonFile> jsonFileList, JsonTask jsonTask) {
        // 删除文件夹下所有的文件
//        String directoryPath = OUTPUT_FILTERED_FILE_PATH + File.separator + jsonTask.getSpecialId() + File.separator + jsonTask.getSingleId();
//        FileUtils.deleteFilesInDirectory(directoryPath);
        Long specialId = jsonTask.getSpecialId();
        Long singleId = jsonTask.getSingleId();
        //切片id
        Long slideId = jsonTask.getSlideId();
        //图片id
        //Long imageId = jsonTask.getImageId();
        //原始图片信息
//        Image image = imageMapper.selectById(imageId);
        //蜡块号
//        String waxCode = image.getWaxCode();
        Long createBy = 0L;
        // 查询切片信息
        SingleSlideSelectBy singleSlideSelectBy = singleSlideMapper.singleSlideBy(singleId);
        singleSlideSelectBy.setCreateBy(createBy);
        int maxZ = calculateZoomLevelRange(Integer.parseInt(singleSlideSelectBy.getWidth()), Integer.parseInt(singleSlideSelectBy.getHeight()));
        singleSlideSelectBy.setMaxZ(maxZ);
        singleSlideSelectBy.setOrganizationId(jsonTask.getOrganizationId());
        // 获取所有瓦片名称列表 z-x-y
        List<String> zoomLevels = tileNameList(Integer.parseInt(singleSlideSelectBy.getWidth()), Integer.parseInt(singleSlideSelectBy.getHeight()), singleSlideSelectBy.getSourceLens());

        ConcurrentMap<String, Geometry> geometryIdMap = new ConcurrentHashMap<>();

        // 创建保存的目录
//        String outputDir = OUTPUT_FILTERED_FILE_PATH + File.separator + specialId + File.separator + singleId;
        String outputDir = OUTPUT_FILTERED_FILE_PATH + File.separator + specialId + File.separator + slideId + File.separator + jsonTask.getCategoryId();
        try {
            Files.createDirectories(Paths.get(outputDir));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (JsonFile jsonFile : jsonFileList) {
            String fileUrl = jsonFile.getFileUrl();
            File f = new File(fileUrl);
            String fileName = f.getName().substring(0, f.getName().lastIndexOf("."));
            if (MapConstant.getStructureSize(jsonTask.getOrganizationId() + fileName) == null) {
                // log.warn("jsonTask id:[{}] singleSlide id:[{}]  structureSize is null； jsonFile:[{}]", jsonTask.getTaskId(), jsonTask.getSingleId(), fileUrl);
                continue;
            }
            jsonParser(f.getPath(), zoomLevels, fileName, singleSlideSelectBy, geometryIdMap, outputDir);
        }
    }

    /**
     * 解析JSON文件并处理地理数据
     *
     * @param inputGeoJsonFilePath 输入的GeoJSON文件路径
     * @param zoomLevels           缩放级别列表
     * @param jsonName             JSON文件名称
     * @param single               单滑块选择器对象
     * @param tileGeometryMap      瓦片几何图形映射表
     * @param outputDir            输出目录路径
     */
    public void jsonParser(String inputGeoJsonFilePath, List<String> zoomLevels, String jsonName, SingleSlideSelectBy single, ConcurrentMap<String, Geometry> tileGeometryMap, String outputDir) {
        JSONArray featuresJson = new JSONArray();
        JsonFactory jsonFactory = new MappingJsonFactory();
        //ObjectMapper mapper = new ObjectMapper();
        JsonToken current = null;
        try (FileInputStream fis = new FileInputStream(inputGeoJsonFilePath); JsonParser jsonParser = jsonFactory.createParser(fis)) {
            if (jsonParser == null) {
                return;
            }
            current = jsonParser.nextToken();
            if (current == null || current != JsonToken.START_OBJECT) {
                return;
            }
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = jsonParser.getCurrentName();
                current = jsonParser.nextToken();
                if ("features".equals(fieldName)) {
                    if (current == JsonToken.START_ARRAY) {
                        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                            try {
                                String node = jsonParser.readValueAsTree().toString();
                                //JsonNode jsonNode = mapper.readTree(node);
                                JSONObject featureObject = JSONObject.parseObject(node);
                                featuresJson.add(featureObject);
                                if (featuresJson.size() > BATCH_SIZE) {
                                    parseJson(featuresJson, zoomLevels, jsonName, single, tileGeometryMap, outputDir);
                                    featuresJson = new JSONArray();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                log.error("filePath:[{}],json-task-parser-service解析任务元数据异常:[{}]", inputGeoJsonFilePath, e.getMessage());
                            }
                        }
                    } else {
                        jsonParser.skipChildren();
                    }
                }
            }
            if (featuresJson.size() > 0) {
                parseJson(featuresJson, zoomLevels, jsonName, single, tileGeometryMap, outputDir);
            }
        } catch (Exception e) {
            log.error("inputGeoJsonFilePath:[{}] ,Unexpected error occurred: [{}]，[{}]", inputGeoJsonFilePath, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void preFeature(List<Features> features, Map<String, String> dynamicDataMap, STRtree tree, Map<Geometry, Features> geometryMap) {
        preFeature(features, dynamicDataMap, tree, geometryMap, "");
    }

    /**
     * 预处理 geometry
     *
     * @param features
     * @param dynamicDataMap
     * @param tree
     * @param geometryMap
     * @param type
     */
    private void preFeature(List<Features> features, Map<String, String> dynamicDataMap, STRtree tree, Map<Geometry, Features> geometryMap, String type) {
        GeometryFactory geometryFactory = new GeometryFactory();
        for (Features element : features) {
            if (dynamicDataMap.containsKey(element.getId())) {
                JSONObject jsonObject = element.getGeometry();
                if (type.equals("10000")) {
                    jsonObject = element.getGeometry10000();
                }
                Object coordinatesObj = jsonObject.getJSONArray("coordinates").get(0);
                // 转换为几何对象
                Geometry geometry = convertToGeometry(coordinatesObj, geometryFactory);
                tree.insert(geometry.getEnvelopeInternal(), geometry);
                geometryMap.put(geometry, element);
            }
        }
    }

    /**
     * 预处理动态数据
     *
     * @param features
     * @return
     */
    private Map<String, String> preDynamicDataMap(List<Features> features, Long specialId) {
        List<String> idList = features.stream().map(Features::getId).collect(Collectors.toList());
        Map<String, String> dynamicDataMap = new HashMap<>();
//        Long seqId = ThreadLocalUtils.get(Constants.TEMP_TABLE_KEY);
        Long seqId = commonJsonParser.getSequenceNumber(specialId);


        Annotation annotation = new Annotation();
        annotation.setSequenceNumber(seqId);
        annotation.setIdList(idList);
        List<Annotation> annotationList = annotationMapper.selectIdList(annotation);
        //将annotationList转map,key为id,value为dynamicData
        if (CollectionUtils.isNotEmpty(annotationList)) {
            annotationList.forEach(x -> {
                dynamicDataMap.put(x.getId(), x.getDynamicData());
            });
        }
        return dynamicDataMap;
    }

    /**
     * 遍历目录中所有得图片
     *
     * @param featuresJson geoJson数据
     * @param zoomLevels   瓦片名称
     * @throws ParseException 解析异常
     * @throws IOException    读取文件异常
     */
    /**
     * 解析JSON数据并处理地理要素信息
     *
     * @param featuresJson    包含地理要素的JSON数组
     * @param zoomLevels      缩放级别列表
     * @param jsonName        JSON文件名称
     * @param single          单滑块选择器对象
     * @param tileGeometryMap 瓦片几何图形映射表
     * @param outputDir       输出目录路径
     */
    public void parseJson(JSONArray featuresJson, List<String> zoomLevels, String jsonName, SingleSlideSelectBy single, ConcurrentMap<String, Geometry> tileGeometryMap, String outputDir) {
        //log.info("singleSlide id:[{}] 轮廓标签:[{}] 轮廓数量:[{}] 开始解析轮廓数据", single.getSingleId(), jsonName, featuresJson.size());
        List<Features> features = featuresJson.toJavaList(Features.class);
        Map<String, String> dynamicDataMap = preDynamicDataMap(features, single.getSpecialId());
        List<String> filteredFilePathList;
        Map<Geometry, Features> geometryMap = new ConcurrentHashMap<>();
        STRtree tree = new STRtree();
        Integer size = MapConstant.getStructureSize(single.getOrganizationId() + jsonName);

        // 对大文件进行特殊处理,单独存储一个文件
        if (size == 1) {
            // 定义大文件目录
            String outputPath = outputDir + "/" + "0-0-0" + ".json";

            // 创建一个Features列表
            List<Features> list = new ArrayList<>();
            // 遍历所有元素
            for (Features feature : features) {
                if (dynamicDataMap.containsKey(feature.getId())) {
                    // 处理单个元素,将json文件中的数据转化为Features对象
                    double resolutions = Double.parseDouble(single.getResolutionX());
                    Features features1 = handleSingleJsonElement(feature, MapConstant.getPathologicalIndicatorCategory(single.getOrganizationId(), jsonName), resolutions, 10, dynamicDataMap.get(feature.getId()));
                    if (features1 != null) {
                        list.add(features1);
                    }
                }
            }
            //log.info("singleSlide id:[{}] 大轮廓处理数量:[{}]", single.getSingleId(), list.size());
            // 写入文件
            writeFilteredGeoJson(list, outputPath);
        } else if (size == 2) {
            preFeature(features, dynamicDataMap, tree, geometryMap);
            // 过滤出符合条件的切片
            int zoom = (int) computeZoomForImageZoom(2.5, single.getSourceLens(), single.getMaxZ());
            // filePathList,获取文件名称,使用-将z-x-y进行分割，并筛选出z等于zoom的切片
            filteredFilePathList = zoomLevels.stream().filter(filePath -> isFirstDigit(filePath, zoom)).collect(Collectors.toList());
            submitPathList(filteredFilePathList, jsonName, tree, geometryMap, single, tileGeometryMap, outputDir, 40, dynamicDataMap);
        } else {
            preFeature(features, dynamicDataMap, tree, geometryMap);
            // 过滤出符合条件的切片
            int zoom = (int) computeZoomForImageZoom(40, single.getSourceLens(), single.getMaxZ());
            // filePathList,获取文件名称,使用-将z-x-y进行分割，并筛选出z等于zoom的切片
            filteredFilePathList = zoomLevels.stream().filter(filePath -> isFirstDigit(filePath, zoom)).collect(Collectors.toList());
            submitPathList(filteredFilePathList, jsonName, tree, geometryMap, single, tileGeometryMap, outputDir, 40, dynamicDataMap);
        }

        if (size == 2 || size == 3) {
            geometryMap = new ConcurrentHashMap<>();
            tree = new STRtree();
            preFeature(features, dynamicDataMap, tree, geometryMap, "10000");
            // 过滤出符合条件的切片
            int zoom = (int) computeZoomForImageZoom(10, single.getSourceLens(), single.getMaxZ());
            // filePathList,获取文件名称,使用-将z-x-y进行分割，并筛选出z等于zoom的切片
            filteredFilePathList = zoomLevels.stream().filter(filePath -> isFirstDigit(filePath, zoom)).collect(Collectors.toList());
            submitPathList(filteredFilePathList, jsonName, tree, geometryMap, single, tileGeometryMap, outputDir, 10, dynamicDataMap);
        }
        // log.info("singleSlide id:[{}] 轮廓标签:[{}] 轮廓数量:[{}] 结束解析轮廓数据", single.getSingleId(), jsonName, features.size());
    }


    /**
     * 提交路径列表以进行处理
     * 该方法负责将一组文件路径提交给线程池进行异步处理每个文件路径都封装在一个识别线程中，
     * 并使用CountDownLatch来确保所有任务完成后再继续执行
     *
     * @param filteredFilePathList 过滤后的文件路径列表，准备处理
     * @param jsonName             JSON名称，用于处理或命名JSON相关操作
     * @param tree                 STR树索引，用于高效查询空间数据
     * @param geometryMap          几何图形映射，关联几何图形和其特征
     * @param single               单滑动选择对象，用于特定的滑动窗口选择逻辑
     * @param tileGeometryMap      瓦片几何图形映射，用于存储和查询瓦片相关的几何图形
     * @param outputDir            输出目录，处理结果的保存位置
     * @param zoom                 缩放级别，用于指定处理的缩放级别
     * @param dynamicDataMap       动态数据映射，用于存储和处理动态数据
     */
    public void submitPathList(List<String> filteredFilePathList, String jsonName, STRtree tree, Map<Geometry, Features> geometryMap, SingleSlideSelectBy single, ConcurrentMap<String, Geometry> tileGeometryMap, String outputDir, int zoom, Map<String, String> dynamicDataMap) {
        // 检查文件路径列表是否非空
        if (CollectionUtils.isNotEmpty(filteredFilePathList)) {
        	long startTime = System.currentTimeMillis();
            // 初始化计数器，用于同步任务完成状态
            CountDownLatch countDownLatch = new CountDownLatch(filteredFilePathList.size());
            // 遍历文件路径列表，提交任务到线程池
            for (String fileName : filteredFilePathList) {
                try {
                    // 提交识别线程到线程池执行
                    EXECUTOR.submit(new RecognitionThread(fileName, jsonName, tree, geometryMap, single, tileGeometryMap, outputDir, zoom, dynamicDataMap, countDownLatch));
                } catch (RejectedExecutionException e) {
                    // 日志记录任务提交错误
                    log.error("Error submitting task: [{}]", e.getMessage());
                }
            }
            try {
                // 等待所有任务完成
                countDownLatch.await();
            } catch (InterruptedException e) {
                // 日志记录等待任务完成时的中断错误，并抛出运行时异常
                log.error("Error waiting for tasks to complete: [{}]", e.getMessage());
                e.printStackTrace();
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            //输出总耗时（单位：毫秒）
            log.info("项目处理所有瓦块的信息如下-专题id:[{}] ,slideId:[{}],singleSlideId:[{}] ，处理的瓦块文件总共： {} 个任务已执行完毕，总耗时: {} ms (约 {:.2f} 秒)", single.getSpecialId(),single.getSlideId(), single.getSingleId(), new Date(),filteredFilePathList.size(),duration, duration / 1000.0);
        }
    }

    /**
     * 读取geojson文件,将文件转化为JSONObject
     *
     * @param filePath 文件路径
     * @return JSONObject
     * @throws IOException 异常
     */
    private static JSONObject readGeoJsonFile(String filePath) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(filePath)), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            log.error("Error reading geojson file: [{}],err msg:[{}]", filePath, e.getMessage());
        }
        return JSONObject.parseObject(sb.toString());
    }

    class RecognitionThread implements Runnable {
        // type 1:标注保存  2：标注修改
        private String fileName;
        private String jsonName;
        private STRtree tree;
        private Map<Geometry, Features> geometryMap;
        private SingleSlideSelectBy single;
        private ConcurrentMap<String, Geometry> tileGeometryMap;
        private String outputDir;
        private int zoom;
        private Map<String, String> dynamicDataMap;
        private CountDownLatch countDownLatch;

        public RecognitionThread(String fileName, String jsonName, STRtree tree, Map<Geometry, Features> geometryMap, SingleSlideSelectBy single, ConcurrentMap<String, Geometry> tileGeometryMap, String outputDir, int zoom, Map<String, String> dynamicDataMap, CountDownLatch countDownLatch) {
            this.fileName = fileName;
            this.jsonName = jsonName;
            this.tree = tree;
            this.geometryMap = geometryMap;
            this.single = single;
            this.tileGeometryMap = tileGeometryMap;
            this.outputDir = outputDir;
            this.zoom = zoom;
            this.dynamicDataMap = dynamicDataMap;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
        	long start = System.nanoTime();
            try {
                // 将名称根据-划分为在z,x,y
                String[] split = fileName.split("-");
                int z = Integer.parseInt(split[0]);
                int x = Integer.parseInt(split[1]);
                int y = Integer.parseInt(split[2]);

                // 获取当前瓦片的边框
                Geometry geometry = tileGeometryMap.get(fileName);
                if (geometry == null) {
                    geometry = calculateZYXExtent(z, x, y, single.getMaxZ());
                    tileGeometryMap.put(fileName, geometry);
                }

                // 获取符合条件的数据
                Envelope envelope = geometry.getEnvelopeInternal();
                List<Geometry> query = tree.query(envelope);
                List<Features> filteredFeatures = new ArrayList<>();
                for (Geometry g : query) {

                    Features features1 = geometryMap.get(g);
                    // 同一层级下，获取数据后删除，避免重复数据产生,
                    if (z == 8 || (z == 6 && MapConstant.getStructureSize(single.getOrganizationId() + jsonName) == 3)) {
                        geometryMap.remove(g);
                    }
                    if (features1 != null) {
                        // 处理单个元素,将json文件中的数据转化为Features对象
                        double resolutions = Double.parseDouble(single.getResolutionX());
                        Features fs = handleSingleJsonElement(features1, MapConstant.getPathologicalIndicatorCategory(single.getOrganizationId(), jsonName), resolutions, zoom, MapUtils.getString(dynamicDataMap, features1.getId()));
                        filteredFeatures.add(fs);
                    }
                }
                if (filteredFeatures.size() > 0) {
                    // 将列表中得数据根据structureSize将structureId分成列表
                    String outputPath = outputDir + "/" + fileName + ".json";
                    // 写入文件
                    writeFilteredGeoJson(filteredFeatures, outputPath);
                    // 计算耗时（秒）
                    long costMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                    long costSeconds = TimeUnit.MILLISECONDS.toSeconds(costMillis);
                    double fileSize = getFileSizeInMB(outputPath);
                    log.info("切分瓦块本地存储专题id:[{}] ,slideId:[{}],singleSlideId:[{}] 。endTime:[{}],存储的瓦块json路径:[{}],文件大小为:[{}M], 耗时: {} 秒", single.getSpecialId(),single.getSlideId(), single.getSingleId(), new Date(),outputPath,fileSize, costSeconds);
                }
            } catch (JSONException e) {
                log.error("Error processing file: [{}]", e.getMessage());
            } finally {
                countDownLatch.countDown();
            }
        }
    }
    
    /**
     * 获取指定文件的大小，单位为 MB（保留两位小数）
     *
     * @param filePath 文件路径（JSON 文件或其他）
     * @return 文件大小（以 MB 为单位），如 5.23 MB；若文件不存在或出错，返回 -1
     */
    public static double getFileSizeInMB(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            log.error("文件路径为空");
            return -1;
        }

        Path path = Paths.get(filePath);

        try {
            if (!Files.exists(path)) {
                log.error("文件不存在: " + filePath);
                return -1;
            }

            if (Files.isDirectory(path)) {
                log.error("路径是一个目录，不是文件: " + filePath);
                return -1;
            }

            long sizeInBytes = Files.size(path);
            return Math.round((sizeInBytes / 1024.0 / 1024.0) * 100.0) / 100.0; // 保留两位小数

        } catch (IOException e) {
            log.error("读取文件大小时发生 I/O 错误: " + e.getMessage());
            return -1;
        }
    }

    /**
     * 根据瓦片索引计算其在原图中的位置范围。
     *
     * @param x         瓦片在当前缩放级别的水平位置
     * @param y         瓦片在当前缩放级别的垂直位置
     * @param zoomLevel 缩放级别
     * @return 瓦片在原图中的位置范围
     */
    public static Geometry calculateZYXExtent(int zoomLevel, int x, int y, int maxZ) {
        // 地图默认瓦片尺寸
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 8307);

        if (zoomLevel > maxZ) {
            zoomLevel = maxZ;
        }

        int trueSize = TILE_SIZE * (int) Math.pow(2, (maxZ - zoomLevel));
        // 计算瓦片的起始坐标
        int sx = x * trueSize;
//        int sy = -(y * trueSize);
//        int sy1 = -(y * trueSize + trueSize);
        int sy = y * trueSize;
        int sy1 = y * trueSize + trueSize;
        Coordinate[] coordinates = new Coordinate[]{new Coordinate(sx, sy), new Coordinate(sx + trueSize, sy), new Coordinate(sx + trueSize, sy1), new Coordinate(sx, sy1), new Coordinate(sx, sy),};

        // 创建Polygon并将其转换为Geometry
        Polygon polygon = factory.createPolygon(coordinates);

        // 将polygon转化为geometry
        return factory.createGeometry(polygon);
    }


    /**
     * 创建文件
     *
     * @param filePath 文件路径
     */
    private static void createFile(String filePath) {
        // 创建File对象
        File file = new File(filePath);
        // 创建文件
        try {
            boolean created = file.createNewFile();
        } catch (IOException e) {
            log.error("An error occurred while creating the file: " + e.getMessage());
        }
    }

    /**
     * 创建文件夹
     *
     * @param directoryPath 文件夹路径
     */
    private static void createFolder(String directoryPath) throws IOException {
        Path path = Paths.get(directoryPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }


    private static Features handleSingleJsonElement(Features element, StructureTag categorySize, double resolution, int zoom, String dynamicData) {
        if (element == null) {
            return null;
        }
        String annotationId = element.getId();
        if (StringUtils.isEmpty(annotationId)) {
            return null;
        }

        JSONObject properties = element.getProperties();
        if (properties == null) {
            return null;
        }
        JSONObject newProperties = new JSONObject();
        newProperties.put("a0", annotationId);
        newProperties.put("a1", "Polygon");
        newProperties.put("a2", "AI");
        newProperties.put("a3", categorySize.getStructureTagId());
        newProperties.put("a4", categorySize.getRgb());
        newProperties.put("a5", categorySize.getStructureTagName());
        newProperties.put("a9", "AI");
        newProperties.put("a11", parseDate(new Date())); // 日期格式化
        newProperties.put("a27", 1);
        newProperties.put("a28", 4);
        newProperties.put("a29", "cell");
        Object dynamicDataObject = null;
        if (StringUtils.isNotEmpty(dynamicData)) {
            dynamicDataObject = JSON.parseObject(dynamicData);
        }
        newProperties.put("a30", dynamicDataObject);

        Features features = new Features();
        features.setId(annotationId);

        JSONObject geometry;
        if (zoom == 10) {
            geometry = element.getGeometry10000();
        } else {
            geometry = element.getGeometry();
        }

        geometry = negateYCoordinates(geometry);

        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 8307);
        WKTReader reader = new WKTReader(geometryFactory);
        //Geometry geom = reader.read(evaluate(geometry.toString()));
        Geometry geom = GeometryUtil.geometryFromJson(geometry.toString());
        // 长度
        newProperties.put("a6", AreaUtils.formattedNumber(String.valueOf(geom.getArea() * resolution * resolution)));
        // 面积
        newProperties.put("a7", AreaUtils.formattedNumber(String.valueOf(geom.getLength() * resolution)));
        features.setGeometry(geometry);
        features.setProperties(newProperties);
        return features;
    }


    /**
     * 将 FastJSON 的 GeoJSON Geometry 对象中所有 coordinates 的 y 坐标取反（加负号）
     * 支持 Polygon 和 MultiPolygon 类型
     *
     * @param geometry JSONObject，格式如 {"type": "Polygon", "coordinates": [...]}
     * @return 新的 JSONObject，y 坐标已取反
     */
    public static JSONObject negateYCoordinates(JSONObject geometry) {
        if (geometry == null || !geometry.containsKey("coordinates")) {
            return geometry;
        }

        String type = geometry.getString("type");
        JSONArray coords = geometry.getJSONArray("coordinates");

        JSONArray transformedCoords = new JSONArray();

        switch (type.toLowerCase()) {
            case "polygon":
                for (int i = 0; i < coords.size(); i++) {
                    JSONArray ring = coords.getJSONArray(i);
                    JSONArray newRing = new JSONArray();
                    for (int j = 0; j < ring.size(); j++) {
                        JSONArray point = ring.getJSONArray(j);
                        double x = point.getDouble(0);
                        double y = -point.getDouble(1);

                        JSONArray newPoint = new JSONArray();
                        newPoint.add(toNumber(x));
                        newPoint.add(toNumber(y));
                        newRing.add(newPoint);
                    }
                    transformedCoords.add(newRing);
                }
                break;

            case "multipolygon":
                for (int i = 0; i < coords.size(); i++) {
                    JSONArray polygon = coords.getJSONArray(i);
                    JSONArray newPolygon = new JSONArray();
                    for (int j = 0; j < polygon.size(); j++) {
                        JSONArray ring = polygon.getJSONArray(j);
                        JSONArray newRing = new JSONArray();
                        for (int k = 0; k < ring.size(); k++) {
                            JSONArray point = ring.getJSONArray(k);
                            double x = point.getDouble(0);
                            double y = -point.getDouble(1);

                            JSONArray newPoint = new JSONArray();
                            newPoint.add(toNumber(x));
                            newPoint.add(toNumber(y));
                            newRing.add(newPoint);
                        }
                        newPolygon.add(newRing);
                    }
                    transformedCoords.add(newPolygon);
                }
                break;

            default:
                throw new IllegalArgumentException("不支持的 geometry 类型: " + type);
        }

        JSONObject result = new JSONObject();
        result.put("type", geometry.getString("type"));
        result.put("coordinates", transformedCoords);
        return result;
    }

    /**
     * 将数字转为最合适的类型：整数用 Integer，小数用 Double
     */
    private static Number toNumber(double value) {
        if (value == (long) value) {
            return (long) value; // 自动转为 long，序列化时不会带 .0
        } else {
            return value; // 保留 double
        }
    }

    /**
     * 写入geojson文件
     *
     * @param list     要写入的元素
     * @param filePath 文件路径
     */
    private static void writeFilteredGeoJson(List<Features> list, String filePath) {
        GeoJson outputGeoJson = new GeoJson();
        if (list.size() > 0) {
            // 判断文件是否存在
            if (!Files.exists(Paths.get(filePath))) {
                // 不存在则创建文件
                createFile(filePath);
                outputGeoJson.setFeatures(list);
                String jsonString = JSON.toJSONString(outputGeoJson);
                exportJson(filePath, jsonString);
            } else {
                // 读取原文件中的内容,并进行追加写入,如果原文件过大，可以使用流分多次取出
                JSONObject jsonObject = readGeoJsonFile(filePath);
                String jsonString = "";
                if (jsonObject == null) {
                    outputGeoJson.setFeatures(list);
                    jsonString = JSON.toJSONString(outputGeoJson);
                } else {
                    JSONArray jsonArray = jsonObject.getJSONArray("features");
                    // 将新数据添加到JSONArray中
                    if (jsonArray != null) {
                        List<Features> features = jsonArray.toJavaList(Features.class);
                        list.addAll(features);
                    }
                    outputGeoJson.setFeatures(list);
                    // 将列表转化为字符串,开始写入文件中
                    jsonString = JSON.toJSONString(outputGeoJson);
                }
                exportJson(filePath, jsonString);
            }
        }
    }


    /**
     * 使用流将字符串写入文件中
     *
     * @param fileUrl    文件路径
     * @param jsonString json字符串
     */
    public static void exportJson(String fileUrl, String jsonString) {
        /*CompletableFuture.runAsync(() -> {
            try {
                // 写入数据量较小时，使用BufferedOutputStream会比OutputStream更快
                // 使用BufferedOutputStream缓冲输出流,将json二进制写入文件中
//                try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(Files.newOutputStream(Paths.get(fileUrl)))) {
//                    bufferedOutputStream.write(jsonString.getBytes());
//                }
                OutputStream outputStream = Files.newOutputStream(Paths.get(fileUrl));
                outputStream.write(jsonString.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });*/
        try {
            OutputStream outputStream = Files.newOutputStream(Paths.get(fileUrl));
            outputStream.write(jsonString.getBytes());
            outputStream.close();
        } catch (Exception e) {
            log.error("Error occurred while writing to file: [{}]; error msg:[{}]", fileUrl, e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 将geojson字符串转换为geojson对象
     *
     * @param geoJson geojson字符串
     * @return geojson对象
     * @throws IOException IO异常
     */
    public static String evaluate(String geoJson) throws IOException {
        String ret;
        GeometryJSON gJson = new GeometryJSON();
        Reader reader = new StringReader(geoJson);
        com.vividsolutions.jts.geom.Geometry geometry = gJson.read(reader);
        ret = geometry.toText();
        return ret;
    }

    /**
     * 将坐标转换为Geometry对象
     *
     * @param coordinatesObj  坐标对象
     * @param geometryFactory 几何工厂
     * @return Geometry对象
     */
    public static Geometry convertToGeometry(Object coordinatesObj, GeometryFactory geometryFactory) {
        if (coordinatesObj instanceof JSONArray) {
            JSONArray coordinates = (JSONArray) coordinatesObj;
            Coordinate[] coordinateArray = new Coordinate[coordinates.size()];
            for (int i = 0; i < coordinates.size(); i++) {
                JSONArray point = coordinates.getJSONArray(i);
                double x = point.getDouble(0);
                double y = point.getDouble(1);
                coordinateArray[i] = new Coordinate(x, y);
            }
            LinearRing linearRing = geometryFactory.createLinearRing(coordinateArray);
            return geometryFactory.createPolygon(linearRing, null); // 第二个参数为空表示没有空洞
        } else {
            throw new IllegalArgumentException("Unsupported geometry type");
        }
    }


    /**
     * 计算图像缩放比
     *
     * @param imageZoom  图像缩
     * @param sourceLens 源长宽比
     * @param maxZ       最大z
     * @return 缩放比
     */
    public static double computeZoomForImageZoom(double imageZoom, int sourceLens, int maxZ) {
        return Math.log(imageZoom / sourceLens) / Math.log(2) + maxZ;
    }


    /**
     * 获取指定目录下的所有文件和子目录名称
     *
     * @param directoryPath 目录路径
     * @return 文件和子目录名称列表
     */
    public static List<String> getFilesAndDirectories(String directoryPath) {
        Path directory = Paths.get(directoryPath);
        List<String> paths = new ArrayList<>();
        try {
            Files.walk(directory).forEach(path -> paths.add(path.getFileName().toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return paths;
    }

    private static boolean isFirstDigit(String filePath, int zoom) {
        String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1);
        Pattern pattern = Pattern.compile("^([0-9])");
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1)) == zoom;
        }
        return false;
    }


    /**
     * 根据当前的z缩放级别计算图像缩放比
     *
     * @param nowZoom 当前缩放级别
     * @param maxZ    最大缩放级别
     * @return long
     */
    private long computeImageZoom(int nowZoom, int maxZ, int sourceLens) {
        return Math.round(Math.pow(2, nowZoom - maxZ) * sourceLens * 100) / 100;
    }

    /**
     * 计算最大缩放级别
     *
     * @param width  宽度
     * @param height 高度
     * @return int
     */
    public static int calculateZoomLevelRange(int width, int height) {
        // 最长边
        int maxNum = Math.max(width, height);

        // 遍历寻找z最大值
        int maxZ = 0;
        while (TILE_SIZE * (1 << maxZ) < maxNum) {
            maxZ++;
        }
        return maxZ;
    }


    /**
     * 获取所有瓦片名称列表
     *
     * @param width      宽度
     * @param height     高度
     * @param sourceLens 源长宽比
     * @return List<String>
     */
    public static List<String> tileNameList(int width, int height, int sourceLens) {
        int zoomLevel = calculateZoomLevelRange(width, height);
        List<Double> zoomLevels1 = Arrays.asList(2.5, 10.0, 40.0);
        List<Integer> zoomLevelList = new ArrayList<>();
        for (Double zoomLevelDouble : zoomLevels1) {
            int computeZoomForImageZoom = (int) computeZoomForImageZoom(zoomLevelDouble, sourceLens, zoomLevel);
            zoomLevelList.add(computeZoomForImageZoom);
        }
        List<String> zoomLevels = new ArrayList<>();
        for (int i : zoomLevelList) {
            List<String> res = calculateTilesForZoom(i, width, height, zoomLevel);
            zoomLevels.addAll(res);
        }
        return zoomLevels;
    }


    /**
     * 计算瓦片名称
     *
     * @param zoomLevel 缩放级别
     * @param width     宽度
     * @param height    高度
     * @param maxLevel  最大z
     * @return List<String>
     */
    public static List<String> calculateTilesForZoom(int zoomLevel, int width, int height, int maxLevel) {
        // 在该z倍率下，一个瓦片对应的实际图像范围
        double trueSize = TILE_SIZE * Math.pow(2, (maxLevel - zoomLevel));
        // 计算X、Y坐标
        int xTiles = (int) Math.ceil(width / trueSize);
        int yTiles = (int) Math.ceil(height / trueSize);
        // 生成所有X、Y坐标
        List<String> tiles = new ArrayList<>();
        for (int x = 0; x < xTiles; x++) {
            for (int y = 0; y < yTiles; y++) {
                tiles.add(zoomLevel + "-" + x + "-" + y);
            }
        }
        return tiles;
    }

    // 根据专题id(必须)+切片id(必须)+脏器id(多个脏器)用于获取脏器文件大小使用
    @Override
    public R<ContourFileVo> getContourJsonSize(Long slideId, Long projectId, List<Long> organTagIds) {
        String filePath = File.separator + "home" + File.separator + "data" + File.separator + "aiJson" + File.separator + projectId;
        List<String> dataFiles = new ArrayList<>();
        long totalSize = 0;
        if (null != slideId) {
            //如果切片id不为空，json目录需要拼接切片id
            filePath = filePath + File.separator + slideId;
            if (CollectionUtils.isNotEmpty(organTagIds)) {
                for (Long tagId : organTagIds) {
                    //如果脏器id不为空，json目录需要拼接脏器id
                    String tagPath = filePath + File.separator + tagId;
                    List<String> files = getFilesDirectory(tagPath);
                    dataFiles.addAll(files);
                }
                totalSize = (long) dataFiles.size();
            }
        }
        // 检测filePath目录是否存在，如果存在，则返回该目录下的所有文件名称，如果不存在，则返回null
        ContourFileVo contourFileVo = ContourFileVo.builder().totalSize(totalSize).build();
        return R.ok(contourFileVo);
    }

    @SuppressWarnings("finally")
    public static List<String> getFilesDirectory(String directoryPath) {
    	// 1. 检查路径是否为空
    	if (directoryPath == null || directoryPath.trim().isEmpty()) {
    		log.error("目录路径:[{}] 为空或 null", directoryPath);
    		return new ArrayList<>(); // 返回空列表，避免调用方出错
    	}

    	Path directory = Paths.get(directoryPath);

    	// 2. 检查目录是否存在
    	if (!Files.exists(directory)) {
    		log.error("目录路径:[{}] 不存在", directoryPath);
    		return new ArrayList<>();
    	}

    	// 3. 检查是否是一个目录
    	if (!Files.isDirectory(directory)) {
    		log.error("路径不是一个目录: " + directoryPath);
    		return new ArrayList<>();
    	}

    	List<String> files = new ArrayList<>();
    	try {
    		// 遍历目录，只收集普通文件
    		files = Files.walk(directory).filter(Files::isRegularFile).map(Path::toString).collect(Collectors.toList());
    	} catch (IOException e) {
    		// 捕获 IO 异常（如权限不足、文件被删除等）
    		log.error("读取目录时发生 IO 错误,目录路径:[{}]", directoryPath);
    		e.printStackTrace();
    		// 返回当前已收集的文件（可能为空）
    	} catch (SecurityException e) {
    		// 防止因权限问题导致崩溃
    		log.error("没有权限访问目录,目录路径:[{}]", directoryPath);
    		e.printStackTrace();
    	} catch (Exception e) {
    		// 兜底异常
    		log.error("未知错误读取目录,目录路径:[{}]", directoryPath);
    		e.printStackTrace();
    	} finally {
    		return files; // 即使出错也返回一个 List（可能为空）
    	}
    }

    //根据专题id(必须)+切片id(必须)+脏器id(非必填)用于单脏器下载使用
    @Override
    public R<cn.staitech.fr.domain.out.JsonFileVo> selectList(Long slideId, Long projectId, Long organTagId) {
        String filePath = File.separator + "home" + File.separator + "data" + File.separator + "aiJson" + File.separator + projectId;
        if (null != slideId) {
            //如果脏器id不为空，json目录需要拼接脏器id
            filePath = filePath + File.separator + slideId;
        }
        if (null != organTagId) {
            //如果脏器id不为空，json目录需要拼接脏器id
            filePath = filePath + File.separator + organTagId;
        }
        // 检测filePath目录是否存在，如果存在，则返回该目录下的所有文件名称，如果不存在，则返回null
        JsonFileVo jsonFileVo = getFilesInDirectory(filePath);
        return R.ok(jsonFileVo);
    }


    public static JsonFileVo getFilesInDirectory(String directoryPath) {
    	// 1. 检查路径是否为空
    	if (directoryPath == null || directoryPath.trim().isEmpty()) {
    		log.error("目录路径:[{}] 为空或 null", directoryPath);
    		return JsonFileVo.builder()
    				.files(new ArrayList<>())
    				.totalSize(0L)
    				.build();
    	}

    	Path directory = Paths.get(directoryPath);

    	// 2. 检查目录是否存在
    	if (!Files.exists(directory)) {
    		log.error("目录路径:[{}] 不存在", directoryPath);
    		return JsonFileVo.builder()
    				.files(new ArrayList<>())
    				.totalSize(0L)
    				.build();
    	}

    	// 3. 检查是否是一个目录
    	if (!Files.isDirectory(directory)) {
    		log.error("路径不是一个目录:[{}]", directoryPath);
    		return JsonFileVo.builder()
    				.files(new ArrayList<>())
    				.totalSize(0L)
    				.build();
    	}

    	// 用于收集文件名和累计大小
    	List<String> files = new ArrayList<>();
    	long[] totalSize = {0}; 

    	try {
    		Files.walk(directory)
    		.filter(Files::isRegularFile)
    		.forEach(path -> {
    			files.add(path.getFileName().toString());
    			try {
    				totalSize[0] += Files.size(path);
    			} catch (IOException e) {
    				log.error("无法读取文件大小,目录路径:[{}]", path);
    			}
    		});
    	} catch (IOException e) {
    		log.error("读取目录时发生 IO 错误,目录路径:[{}]", directoryPath);
    		e.printStackTrace();
    		// 出错后仍返回已收集的数据（可能为空）
    	} catch (SecurityException e) {
    		log.error("没有权限访问目录,目录路径:[{}]", directoryPath);
    		e.printStackTrace();
    	}

    	// 构建并返回结果
    	return JsonFileVo.builder()
    			.files(files)
    			.totalSize(totalSize[0])
    			.build();
    }
}



