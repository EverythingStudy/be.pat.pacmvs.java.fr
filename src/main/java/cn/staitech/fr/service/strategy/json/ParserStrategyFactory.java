package cn.staitech.fr.service.strategy.json;

import com.google.common.collect.Maps;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author: wangfeng
 * @create: 2024-05-10 14:09:51
 * @Description: Json ParserStrategy Factory
 */
@Component
@Data
public class ParserStrategyFactory {

    /**
     * 把策略角色（类型）key,和参数value放到Map中
     * key就是beanName(具体策略实现类中@Component的名字)，value就是接口（具体的实现类）
     * Maps是guava下的封装类型，实则是静态的创建了一个HashMap的对象，Maps可以根据key去获取value对象
     */
    public final Map<String, ParserStrategy> parserStrategyHashMap = Maps.newHashMapWithExpectedSize(128);

    /**
     * 利用构造函数在项目启动的时候将策略实现类注册到 map里
     *
     * @param strategyMap
     */
    public ParserStrategyFactory(Map<String, ParserStrategy> strategyMap) {
        this.parserStrategyHashMap.clear();
        this.parserStrategyHashMap.putAll(strategyMap);
    }

    /**
     * @param algorithmCode 算法标识
     * @return 解析器
     */
    public ParserStrategy getParserStrategy(String algorithmCode) {
        return parserStrategyHashMap.get(algorithmCode);
    }

}
