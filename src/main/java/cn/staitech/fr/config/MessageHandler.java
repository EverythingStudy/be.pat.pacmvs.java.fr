package cn.staitech.fr.config;

import cn.staitech.fr.service.strategy.json.JsonTaskParserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * @author mugw
 * @version 1.0
 * @description 2024-05-18需求：
 * 1、业务方法在mq消费者线程中同步执行。
 * 2、新增重试队列，业务方法异常时入重试队列。
 * @date 2024/5/16 14:55:40
 */
@Component
@Slf4j
public class MessageHandler {

    @Value("${queues.algoMsgRetry:algo.message.retry.queue}")
    private String ALGO_MSG_RETRY_QUEUE;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private JsonTaskParserService jsonTaskParserService;

    @RabbitListener(queues = "${queues.algoMsg:test2}")
    public void handleMessage(Message message) {
        String msg = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("MessageHandler received: {}", msg);
        processMessage(msg);
    }

    public void processMessage(String message) {
        try {
            log.info("业务开始处理消息: {}", message);
            jsonTaskParserService.input(message);
            log.info("业务处理完成: {}", message);
        } catch (Exception e) {
            log.error("业务解析消息异常：[{}]，消息内容：[{}]", e.getMessage(), message);
            rabbitTemplate.convertAndSend(ALGO_MSG_RETRY_QUEUE, message);
            log.info("消息发送重试队列成功");
        }
    }
}
