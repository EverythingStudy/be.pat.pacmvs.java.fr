package cn.staitech.fr.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Component;

/**
 *  @author staitech
 *  @since : 2024/07/18
 * @version :v0.0.1
 */
@Component
public class InitializinConfig implements InitializingBean {
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    /**
     * @see LettuceConnectionFactory
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (redisConnectionFactory instanceof LettuceConnectionFactory) {
            LettuceConnectionFactory lettuceConnectionFactory = (LettuceConnectionFactory) redisConnectionFactory;
            lettuceConnectionFactory.setValidateConnection(true);
        }
    }

}