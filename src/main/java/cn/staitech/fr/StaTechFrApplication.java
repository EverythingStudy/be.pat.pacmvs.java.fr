package cn.staitech.fr;

import cn.hutool.extra.spring.EnableSpringUtil;
import cn.staitech.common.security.annotation.EnableCustomConfig;
import cn.staitech.common.security.annotation.EnableRyFeignClients;
import cn.staitech.common.swagger.annotation.EnableCustomSwagger2;
import cn.staitech.fr.utils.MessageSource;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.TimeZone;

/**
 * @author staitech
 */
@EnableSpringUtil
@EnableCustomConfig
@EnableCustomSwagger2
@EnableRyFeignClients
@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
@EnableTransactionManagement
@MapperScan({"cn.staitech.fr.mapper"})
public class StaTechFrApplication {

    public StaTechFrApplication(org.springframework.context.MessageSource messageSource) {
        MessageSource.init(messageSource);
    }

    public static void main(String[] args) {
        //jvm参数设置时间 -Duser.timezone="Asia/Shanghai"
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(StaTechFrApplication.class, args);
        System.out.println("数字阅片模块启动成功");

    }

    @Bean
    public MybatisPlusInterceptor paginationInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return mybatisPlusInterceptor;

    }

}
