package cn.staitech.fr.config;

import cn.staitech.fr.feign.PythonService;
import cn.staitech.fr.vo.annotation.GenerateThumbnail;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class AsyncTask {

    @Resource
    private PythonService pythonService;

    @SneakyThrows
    @Async("getAsyncExecutor")
    public void generateThumbnail(Long slideId, Long categoryId, String imageUrl, List<JSONObject> contourList,int type) {
        GenerateThumbnail generateThumbnail = new GenerateThumbnail();
        generateThumbnail.setSlideId(slideId);
        generateThumbnail.setCategoryId(categoryId);
        generateThumbnail.setSlideRoiPolygon(contourList);
        generateThumbnail.setSvsPath(imageUrl);
        generateThumbnail.setTypes(type);
        pythonService.generateThumbnail(generateThumbnail);

    }


}
