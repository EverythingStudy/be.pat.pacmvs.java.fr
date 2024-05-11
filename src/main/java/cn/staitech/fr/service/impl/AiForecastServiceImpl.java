package cn.staitech.fr.service.impl;

import cn.staitech.fr.domain.Annotation;
import cn.staitech.fr.domain.Image;
import cn.staitech.fr.domain.SingleSlide;
import cn.staitech.fr.domain.out.SingleSlideSelectBy;
import cn.staitech.fr.mapper.AnnotationMapper;
import cn.staitech.fr.mapper.ImageMapper;
import cn.staitech.fr.mapper.SingleSlideMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.staitech.fr.domain.AiForecast;
import cn.staitech.fr.service.AiForecastService;
import cn.staitech.fr.mapper.AiForecastMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author admin
 * @description 针对表【fr_ai_forecast】的数据库操作Service实现
 * @createDate 2024-04-09 14:42:38
 */
@Service
public class AiForecastServiceImpl extends ServiceImpl<AiForecastMapper, AiForecast> implements AiForecastService {


    @Resource
    private AnnotationMapper annotationMapper;

    @Resource
    private SingleSlideMapper singleSlideMapper;

    @Resource
    private ImageMapper imageMapper;


    @Override
    public Boolean forecastResults(Long singleSlideId, Long imageId) {
        try {
            if (!Optional.ofNullable(singleSlideId).isPresent()) {
                return false;
            }
            Annotation annotation = new Annotation();
            annotation.setSingleSlideId(singleSlideId);
            annotation.setFiligreeContour(true);
            Annotation annotationBy = annotationMapper.getOrganArea(annotation);
            if (annotationBy == null) {
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
            double areas = Double.parseDouble(annotationBy.getArea()) * resolutions * resolutions;
            String area = String.format("%.3f", areas);
            annotationBy.setArea(area);
            SingleSlide singleSlide = new SingleSlide();
            singleSlide.setSingleId(singleSlideId);
            singleSlide.setArea(annotationBy.getArea());
            int res = singleSlideMapper.updateById(singleSlide);
            if (res > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }


    }

    @Override
    public void indicatorCount(Long singleSlideId,Long categoryId,String jsonCode) {
        List<AiForecast> aiForecasts = new ArrayList<>();

        Map<String, String[]> indicatorResultsMap = new HashMap<>();

        // 结构面积
        /*Annotation annotation = new Annotation();
        annotation.setSequenceNumber(0L);
        annotation.setSingleSlideId(singleSlideId);//单脏器切片id
        annotation.setCategoryId(categoryId);// 标注类别ID
        Annotation structureArea = annotationMapper.getStructureArea(annotation);*/
        // 精细轮廓总面积
        SingleSlide singleSlide = singleSlideMapper.selectById(singleSlideId);

        /*indicatorResultsMap.put("导管占比", new String[]{"Duct area%","", ""});
        indicatorResultsMap.put("腺泡细胞核密度", new String[]{"Nucleus density of acinus","", ""});
        indicatorResultsMap.put("上皮顶部胞质占比", new String[]{"Epithelial apex cytoplasm area%","", ""});
        indicatorResultsMap.put("间质占比", new String[]{"Mesenchyme area%","", ""});
        indicatorResultsMap.put("腺泡占比", new String[]{"Acinus area%","", ""});
        indicatorResultsMap.put("腺泡细胞核面积（单个）", new String[]{"Acinar nucleus area (per)","", ""});*/
        indicatorResultsMap.put("泪腺面积", new String[]{"Lacrimal gland area",singleSlide.getArea(), "平方毫米"});

        for (Map.Entry<String, String[]> entry : indicatorResultsMap.entrySet()) {
            String indicatorCode = entry.getKey();
            String englishName = entry.getValue()[0];
            String result = entry.getValue()[1];
            String unit = entry.getValue()[2];

            AiForecast forecast = new AiForecast();
            forecast.setSingleSlideId(singleSlideId);
            forecast.setQuantitativeIndicators(indicatorCode);
            forecast.setResults(result);
            forecast.setUnit(unit);

            aiForecasts.add(forecast);
        }

    }


}




