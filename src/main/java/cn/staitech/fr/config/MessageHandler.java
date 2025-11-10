package cn.staitech.fr.config;

import cn.staitech.fr.domain.JsonTask;
import cn.staitech.fr.domain.dto.DelayMessageDTO;
import cn.staitech.fr.service.JsonTaskService;
import cn.staitech.fr.service.strategy.json.JsonTaskParserService;
import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
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
    @Resource
    private JsonTaskService jsonTaskService;

    @RabbitListener(queues = "${queues.algoMsg:test2}")
    public void handleMessage(Message message, Channel channel) {
        TraceContext.generateTraceId();
        String msg = new String(message.getBody(), StandardCharsets.UTF_8);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info("MessageHandler received: {}", msg);
        try {
            processMessage(msg);
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            log.info("消息处理完成并已确认: {}", msg);
        } catch (Exception e) {
            log.error("消息处理失败：[{}]，消息内容：[{}]", e.getMessage(), msg);
            try {
                // 发送到重试队列
                rabbitTemplate.convertAndSend(ALGO_MSG_RETRY_QUEUE, msg);
                log.info("消息发送重试队列成功");
                // 确认原消息
                channel.basicAck(deliveryTag, false);
            } catch (Exception sendException) {
                log.error("发送重试队列失败: {}", sendException.getMessage());
                // 拒绝消息并重新入队
                try {
                    channel.basicNack(deliveryTag, false, true);
                } catch (Exception nackException) {
                    log.error("拒绝消息失败: {}", nackException.getMessage());
                }
            }
        } finally {
            TraceContext.clear();
        }
    }

    public void processMessage(String message) throws Exception {
        try {
            log.info("业务开始处理消息: {}", message);
            jsonTaskParserService.input(message);
            log.info("业务处理完成: {}", message);
        } catch (Exception e) {
            log.error("业务解析消息异常：[{}]，消息内容：[{}]", e.getMessage(), message);
            throw e; // 重新抛出异常以便上层处理
        }
    }

    /**
     * 发送延迟消息
     *
     * @param message           消息内容
     * @param delayMilliseconds 延迟时间（毫秒）
     */
    public void sendDelayedMessage(String message, long delayMilliseconds) {
        // 设置延迟时间
        rabbitTemplate.convertAndSend("delayed.exchange", "delay.check.routing.key", message, msg -> {
            msg.getMessageProperties().setHeader("x-delay", delayMilliseconds);
            return msg;
        });
    }

    @RabbitListener(queues = "task.delay.check.queue")
    public void handleDelayedMessage(String data, Channel channel, Message message) {
        TraceContext.generateTraceId();
        DelayMessageDTO delayMessageDTO = JSON.parseObject(data, DelayMessageDTO.class);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        // 处理延迟任务检查逻辑
        log.info("接收到延迟消息，singleId: {}", delayMessageDTO.getSingleId());
        JsonTask jsonTask = new JsonTask();
        jsonTask.setSingleId(Long.parseLong(delayMessageDTO.getSingleId()));
        jsonTaskService.checkTask(jsonTask);
        // 手动确认消息
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("延迟消息处理失败: singleId={}, error={}", delayMessageDTO.getSingleId(), e.getMessage());
            try {
                // 拒绝消息并重新入队
                channel.basicNack(deliveryTag, false, true);
            } catch (Exception nackException) {
                log.error("拒绝延迟消息失败: {}", nackException.getMessage());
            }
        } finally {
            TraceContext.clear();
        }
        log.info("延迟消息处理完成并已确认: {}", delayMessageDTO.getSingleId());
    }
}