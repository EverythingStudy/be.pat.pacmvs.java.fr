package cn.staitech.fr.component;

import cn.staitech.fr.service.strategy.json.JsonTaskParserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author mugw
 * @version 1.0
 * @description
 *      2024-05-18需求：
 *      1、业务方法在mq消费者线程中同步执行。
 *      2、新增重试队列，业务方法异常时入重试队列。
 * @date 2024/5/16 14:55:40
 */
@Component
@Slf4j
public class MessageRetryHandler {

    @Resource
    private JsonTaskParserService jsonTaskParserService;

    public void handleMessage(Object message) {
        String msg = message.toString();
        if (message instanceof byte[]){
            msg = new String((byte[]) message);
        }
        log.info("MessageRetryHandler received: {}" , msg);
        processMessage(msg);
    }

    public void processMessage(String message) {
        try {
            log.info("业务开始处理消息: {}" , message);
            jsonTaskParserService.input(message);
            log.info("业务处理完成: {}" , message);
        } catch (Exception e) {
            log.error("业务解析消息异常：[{}]，消息内容：[{}]",e.getMessage(),message);
        }
    }
}
