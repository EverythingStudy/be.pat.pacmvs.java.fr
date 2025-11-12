package cn.staitech.fr.config;

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

    @Value("${queues.algoMsg:algo.message.queue}")
    private String ALGO_MSG_QUEUE;

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String message) {
        try {
            // 设置消息属性，如需持久化可以设置消息类型为AMQP.BasicProperties.Type.PERSISTENT_TEXT_PLAIN
            rabbitTemplate.convertAndSend(ALGO_MSG_QUEUE, message);
            log.info("生产者发送消息成功: " + message);
        } catch (Exception e) {
            log.error("生产者发送消息失败: " + e.getMessage(), e);
            throw new RuntimeException("消息发送失败", e);
        }
    }

    public void sendDelayedMessage(String message, long delayMilliseconds) {
        // 设置延迟时间
        rabbitTemplate.convertAndSend("delayed.exchange", "delay.check.routing.key", message, msg -> {
            msg.getMessageProperties().setHeader("x-delay", delayMilliseconds);
            return msg;
        });
    }


}
