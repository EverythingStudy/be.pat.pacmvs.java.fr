package cn.staitech.fr.feign;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "phtyon",url = "${forward.phyonUrl}")
public interface PythonService {


    @PostMapping(value = "/get_label",consumes= MediaType.APPLICATION_JSON_VALUE)
    JSONObject getLabel(JSONObject labelEntity);





}
