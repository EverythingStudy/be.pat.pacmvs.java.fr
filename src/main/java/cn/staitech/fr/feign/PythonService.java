package cn.staitech.fr.feign;

import cn.staitech.fr.vo.annotation.GenerateThumbnail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "python",url = "${forward.pythonUrl}")
public interface PythonService {

    @PostMapping(value = "Generate_thumbnail/")
    void generateThumbnail(@RequestBody GenerateThumbnail generateThumbnail);


}
