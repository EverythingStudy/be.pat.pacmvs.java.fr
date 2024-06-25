package cn.staitech.fr.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Component;


@Component
public class InitializinConfig  implements InitializingBean {
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    /**
     * @see org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if(redisConnectionFactory instanceof LettuceConnectionFactory){
            LettuceConnectionFactory lettuceConnectionFactory = (LettuceConnectionFactory)redisConnectionFactory;
            lettuceConnectionFactory.setValidateConnection(true);
        }
    }

}