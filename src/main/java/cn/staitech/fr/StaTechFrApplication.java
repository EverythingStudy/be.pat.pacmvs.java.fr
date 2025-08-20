package cn.staitech.fr;

import cn.hutool.extra.spring.EnableSpringUtil;
import cn.staitech.common.security.annotation.EnableCustomConfig;
import cn.staitech.common.security.annotation.EnableRyFeignClients;
import cn.staitech.common.swagger.annotation.EnableCustomSwagger2;
import cn.staitech.fr.utils.MessageSource;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
@Slf4j
public class StaTechFrApplication {

    public StaTechFrApplication(org.springframework.context.MessageSource messageSource) {
        MessageSource.init(messageSource);
    }

    public static void main(String[] args) throws UnknownHostException {
        //jvm参数设置时间 -Duser.timezone="Asia/Shanghai"
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        ConfigurableApplicationContext application = SpringApplication.run(StaTechFrApplication.class, args);
        Environment env = application.getEnvironment();
        log.info(" Doc: http://{}:{}/doc.html", InetAddress.getLocalHost().getHostAddress(), env.getProperty("server.port"));
        System.out.println("数字阅片模块启动成功");
    }

    @Bean
    public MybatisPlusInterceptor paginationInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return mybatisPlusInterceptor;

    }

}
