package cn.staitech.fr.component;
import com.rabbitmq.client.GetResponse;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
/**
 * @author mugw
 * @version 1.0
 * @description
 * @date 2024/5/10 14:58:12
 */


@Component
public class ParkDataPuller {

    private final RabbitTemplate rabbitTemplate;

    public ParkDataPuller(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void pullMessage() {

        // 使用ChannelCallback执行异步操作
        rabbitTemplate.execute(channel -> {
            try {
                // basicGet方法，第一个参数是队列名，第二个参数是是否自动确认
                GetResponse result = channel.basicGet("parkdata", true);
                if (result != null) {
                    byte[] body = result.getBody();
                    String message = new String(body, StandardCharsets.UTF_8);
                    System.out.println("消费者拉取消息: " + message);
                    // 在这里处理接收到的消息
                } else {
                    System.out.println("队列中没有消息");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}
