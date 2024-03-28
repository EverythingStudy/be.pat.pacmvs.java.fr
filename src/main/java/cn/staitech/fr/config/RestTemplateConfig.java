package cn.staitech.fr.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 
* @ClassName: RestTemplateConfig
* @Description:
* @author wanglibei
* @date 2023年8月24日
* @version V1.0
 */
@Configuration
public class RestTemplateConfig { 

    @Bean
    RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
