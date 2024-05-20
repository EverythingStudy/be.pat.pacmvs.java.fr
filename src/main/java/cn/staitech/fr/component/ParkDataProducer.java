package cn.staitech.fr.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author mugw
 * @version 1.0
 * @description
 * @date 2024/5/10 10:41:39
 */

@Slf4j
@Component
public class ParkDataProducer {

    @Value("${queues.algoMsg:algo.message.queue.local}")
    private String ALGO_MSG_QUEUE;

    @Resource
    private  RabbitTemplate rabbitTemplate;

    public void sendMessage(String message) {

        // 设置消息属性，如需持久化可以设置消息类型为AMQP.BasicProperties.Type.PERSISTENT_TEXT_PLAIN
        rabbitTemplate.convertAndSend(ALGO_MSG_QUEUE, message);
        log.info("生产者发送消息成功: " + message);
    }
}
