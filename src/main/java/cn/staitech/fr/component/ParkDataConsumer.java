package cn.staitech.fr.component;

import cn.staitech.fr.service.strategy.json.JsonTaskParserService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
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

    @Resource
    private JsonTaskParserService jsonTaskParserService;

    @RabbitListener(queues = "parkdata", ackMode = "MANUAL")
    public void consumeParkData(Message message, Channel channel) throws IOException {
        try {
            String receivedMessage = new String(message.getBody(), StandardCharsets.UTF_8);
            log.info("消费者收到消息: " + receivedMessage);
            // 成功处理后手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            // 处理消息逻辑...
            processParkData(receivedMessage);

        } catch (Exception e) {
            // 出现异常时可以选择拒绝消息，以便重试或死信队列处理
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

    // 示例方法，实际应用中根据业务逻辑处理数据
    private void processParkData(String data) {
        jsonTaskParserService.input(data);
    }
}
