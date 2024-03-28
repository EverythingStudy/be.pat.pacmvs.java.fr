package cn.staitech.fr.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FastJsonConfig {
    static {
        /**
         * 全局配置关闭 Fastjson 循环引用
         */
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.DisableCircularReferenceDetect.getMask();
    }
}
