package cn.staitech.fr.component;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 项目重新部署，启动时只执行一次
 *
 * @author wangfeng
 */
@Component
public class Starter {

    @PostConstruct
    public void init() {

    }

}

