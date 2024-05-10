package cn.staitech.fr.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * @author mugw
 * @version 1.0
 * @description
 * @date 2024/5/10 10:53:52
 */

@Slf4j
@Component
public class ParkDataConsumer {

    @RabbitListener(queues = "parkdata")
    public void consumeParkData(Message message) {
        String receivedMessage = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("消费者收到消息: " + receivedMessage);

        // 这里处理接收到的消息，例如解析并保存数据到数据库
        // processParkData(receivedMessage);
    }

    // 示例方法，实际应用中根据业务逻辑处理数据
    private void processParkData(String data) {
        // ...
    }
}
