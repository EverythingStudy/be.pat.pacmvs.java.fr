package cn.staitech.fr;

import cn.staitech.common.security.annotation.EnableCustomConfig;
import cn.staitech.common.security.annotation.EnableRyFeignClients;
import cn.staitech.common.swagger.annotation.EnableCustomSwagger2;
import cn.staitech.fr.netty.websocket.NioWebSocketServer;
import cn.staitech.fr.utils.MessageSource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.TimeZone;

/**
 * 系统模块 .
 *
 * @author staitech
 * @EnableFeignClients 此注解报错：Field remoteLogService in cn.staitech.common.log.service.AsyncLogService required a bean of
 * type 'cn.staitech.system.api.RemoteLogService' that could not be found.
 */
@EnableCustomConfig
@EnableCustomSwagger2
@EnableRyFeignClients
@SpringBootApplication
@EnableDiscoveryClient
@EnableTransactionManagement
@MapperScan({"cn.staitech.fr.mapper"})
@EnableElasticsearchRepositories(basePackages = {"cn.staitech.common.log.elasticsearchRepositories"})
public class StaiTechFrApplication {


    public StaiTechFrApplication(org.springframework.context.MessageSource messageSource) {
        MessageSource.init(messageSource);
    }

    public static void main(String[] args) {
        //jvm参数设置时间 -Duser.timezone="Asia/Shanghai"
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(StaiTechFrApplication.class, args);
        System.out.println("智能阅片模块启动成功");
        new NioWebSocketServer().start();

    }
}
