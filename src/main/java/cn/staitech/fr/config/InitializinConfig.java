package cn.staitech.fr.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;


@Component
public class InitializinConfig implements InitializingBean {
    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    /**
     * @see LettuceConnectionFactory
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if(redisConnectionFactory instanceof LettuceConnectionFactory){
            LettuceConnectionFactory lettuceConnectionFactory = (LettuceConnectionFactory)redisConnectionFactory;
            lettuceConnectionFactory.setValidateConnection(true);
        }
    }

}