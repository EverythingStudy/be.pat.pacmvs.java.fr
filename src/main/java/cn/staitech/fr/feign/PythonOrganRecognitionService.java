package cn.staitech.fr.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import cn.staitech.fr.vo.annotation.AiAlgorithm;
import cn.staitech.fr.vo.annotation.StartRecognition;

@FeignClient(name = "pythonOrganRecognition",url = "${forward.organRecognitionUrl}")
public interface PythonOrganRecognitionService {

    @PostMapping(value = "CreateGPUAIPepost/")
    String startPrediction(@RequestBody StartRecognition startRecognition);
    
    @PostMapping(value = "CreateGPUAIPepost/")
    String algorithm(@RequestBody AiAlgorithm aiAlgorithm);


}
