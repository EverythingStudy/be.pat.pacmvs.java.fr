package cn.staitech.fr.config;

import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Time;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AsyncTask {

    @SneakyThrows
    @Async("getAsyncExecutor")
    public void asyncSave(Long slideId, Long categoryId, String imageUrl, List<JSONObject> contourList,int type) {

        Thread.sleep(5000);


        System.out.println("---------------------------->");

    }


}
