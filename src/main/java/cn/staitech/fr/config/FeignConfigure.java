package cn.staitech.fr.config;

import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author .
 */
@Configuration
public class FeignConfigure {

    /**
     * FeignClient的默认超时时间为10s，不会开启重试机制，需要自定义配置
     *
     * @return
     */
    @SuppressWarnings("deprecation")
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(5000, 5000);
    }

    /**
     * 自定义重试次数
     *
     * @param @return
     * @return Retryer
     * @throws
     * @Title: feignRetryer
     * @Description: 重试3次-超时时间设置,开启重试机制，默认为5次（包含首次请求）
     */
    @Bean
    public Retryer feignRetryer() {
        Retryer retryer = new Retryer.Default(2000, 3000, 4);
        return retryer;
    }
}