package cn.staitech.fr.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.staitech.fr.domain.*;
import cn.staitech.fr.enums.JsonTaskStatusEnum;
import cn.staitech.fr.mapper.*;
import cn.staitech.fr.service.JsonTaskService;
import cn.staitech.fr.service.SingleSlideService;
import cn.staitech.fr.service.strategy.json.JsonTaskParserService;
import com.alibaba.ttl.TtlRunnable;
import com.alibaba.ttl.threadpool.TtlExecutors;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SingleSlideServiceImpl extends ServiceImpl<SingleSlideMapper, SingleSlide> implements SingleSlideService {

    Executor executor = new ThreadPoolExecutor(2, 20, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1000), new ThreadPoolExecutor.DiscardOldestPolicy());

    // 包装线程池
    Executor ttlExecutor = TtlExecutors.getTtlExecutor(executor);
    @Resource
    private AnnotationMapper annotationMapper;

    @Resource
    private SingleSlideMapper singleSlideMapper;

    @Resource
    private ImageMapper imageMapper;

    @Resource
    private OrganTagMapper organTagMapper;
    @Resource
    private JsonTaskService jsonTaskService;
    @Resource
    @Lazy
    private JsonTaskParserService jsonTaskParserService;
    @Autowired
    private JsonFileMapper jsonFileMapper;


    @Override
    public Boolean forecastResults(Long singleSlideId, Long imageId) {
        try {
            if (!Optional.ofNullable(singleSlideId).isPresent()) {
                return false;
            }
            SingleSlide singleSlideBy = singleSlideMapper.selectById(singleSlideId);
            if (singleSlideBy == null) {
                return false;
            }
            // 查询详情信息
            Annotation annotation = new Annotation();
            annotation.setSingleSlideId(singleSlideId);
            annotation.setFiligreeContour(true);

            OrganTag category = organTagMapper.selectById(singleSlideBy.getCategoryId());
            if (category != null && Objects.equals(category.getOrganTagCode(), "08") && category.getSpeciesId() == "1") {
                // 甲状旁腺
                annotation.setCategoryId(singleSlideBy.getCategoryId());
            }
            Annotation annotationBy = annotationMapper.getOrganArea(annotation);

            if (category != null && Objects.equals(category.getOrganTagCode(), "07") && category.getSpeciesId() == "1") {
                // 甲状腺
                annotationBy = annotationMapper.unionGeometryArea(singleSlideId);
            }
            if (annotationBy == null || annotationBy.getArea() == null) {
                return false;
            }
            // 查询图像分辨率
            Image image = imageMapper.selectById(imageId);
            if (image == null) {
                return false;
            }
            if (!Optional.ofNullable(image.getResolutionX()).isPresent()) {
                return false;
            }
            double resolutions = Double.parseDouble(image.getResolutionX());
            double areas = (Double.parseDouble(annotationBy.getArea()) * resolutions * resolutions) * 0.000001;
            BigDecimal bd1 = new BigDecimal(Double.toString(areas));
            bd1 = bd1.setScale(9, RoundingMode.HALF_UP);
            String area = bd1.toPlainString();
            double perimeters = (Double.parseDouble(annotationBy.getPerimeter()) * resolutions) * 0.001;
            BigDecimal bd = new BigDecimal(Double.toString(perimeters));
            bd = bd.setScale(9, RoundingMode.HALF_UP);
            String perimeter = bd.toPlainString();
            annotationBy.setArea(area);
            SingleSlide singleSlide = new SingleSlide();
            singleSlide.setSingleId(singleSlideId);
            singleSlide.setArea(annotationBy.getArea());
            singleSlide.setPerimeter(perimeter);
            int res = singleSlideMapper.updateById(singleSlide);
            if (res > 0) {
                JsonTask jsonTask = jsonTaskService.getOne(new LambdaQueryWrapper<>(JsonTask.class).eq(JsonTask::getSingleId, singleSlideId));
                if (!Objects.isNull(jsonTask) && JsonTaskStatusEnum.PARSE_NOT_START.getCode().equals(jsonTask.getStatus())) {
                    Date startTime = new Date();
                    log.info("jsonTask id:{} singleSlide id:{} checkJson 精细轮廓进入指标开始 startTime:{}", jsonTask.getTaskId(), jsonTask.getSingleId(), DateUtil.formatDateTime(startTime));
                    List<JsonFile> fileList = jsonFileMapper.selectList(Wrappers.<JsonFile>lambdaQuery().eq(JsonFile::getTaskId, jsonTask.getTaskId()).eq(JsonFile::getAiStatus, 0).isNotNull(JsonFile::getFileUrl));
                    ttlExecutor.execute(Objects.requireNonNull(TtlRunnable.get(() -> {
                        jsonTaskParserService.structureFileCalculate(jsonTask, fileList);
                    })));
                    log.info("jsonTask id:{} singleSlide id:{} checkJson 精细轮廓进入指标结束 endTime:{}", jsonTask.getTaskId(), jsonTask.getSingleId(), DateUtil.between(startTime, new Date(), DateUnit.SECOND));
                }
//                Map<String, List<OrganStructureConfig.OrganStructure>> outline = organStructureConfig.getOutline();
//                List<OrganStructureConfig.OrganStructure> organStructureList = outline.get(category.getOrganTagCode());
//                if (!CollectionUtils.isEmpty(organStructureList)) {
//                    JsonTask task = new JsonTask();
//                    task.setSingleId(singleSlideId);
//                    OutlineCustom parser = mapOutline.get(jsonTask.getAlgorithmCode());
//                    parser.getCustomOutLine(task);
//                }
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("forecastResults异常:{}", ex.getMessage());
            return false;
        }
    }
}