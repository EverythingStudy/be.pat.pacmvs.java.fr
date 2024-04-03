package cn.staitech.fr.feign;

import com.alibaba.fastjson.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "phtyon",url = "${forward.phyonUrl}")
public interface PythonService {

    @PostMapping(value = "/Generate_thumbnail")
    void generateThumbnail(
            @RequestParam("slideId") Long slideId,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("svsPath") String svsPath,
            @RequestParam("slideRoiPolygon")List<JSONObject> slideRoiPolygon,
            @RequestParam("types")int types);


}
