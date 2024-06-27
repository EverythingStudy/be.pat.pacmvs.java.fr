package cn.staitech.fr.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mugw
 * @version 1.0
 * @description
 * @date 2024/5/16 14:14:12
 */

@Configuration
public class DynamicRabbitConfig {
    @Value("${queues.algoMsg:algo.message.queue}")
    private String ALGO_MSG_QUEUE;
    @Value("${queues.algoMsgRetry1:algo.message.retry.queue}")
    private String ALGO_MSG_RETRY_QUEUE;

    private final ConnectionFactory connectionFactory;
    private final ApplicationContext applicationContext;


    @Autowired
    public DynamicRabbitConfig(ConnectionFactory connectionFactory, ApplicationContext applicationContext) {
        this.connectionFactory = connectionFactory;
        this.applicationContext = applicationContext;
    }

    private Map<String, SimpleMessageListenerContainer> containers = new HashMap<>();

    @PostConstruct
    public void init() {
        int count = Runtime.getRuntime().availableProcessors() / 2;
        if (count < 8) count = 8;
        for (int i = 0; i < count; i++) {
            registerListener(ALGO_MSG_QUEUE, "messageHandler");
            registerListener(ALGO_MSG_RETRY_QUEUE, "messageRetryHandler");
        }
    }

    /**
     * 注册一个消息监听器到指定的队列。
     *
     * @param queueName 需要监听的队列名称。
     * @param handlerClass 处理消息的处理器类的名称，该类需要在应用上下文中注册。
     *
     * 该方法不返回任何内容。
     */
    public void registerListener(String queueName, String handlerClass) {
        // 创建一个SimpleMessageListenerContainer实例用于监听消息
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(queueName); // 设置监听的队列名称
        // 创建一个MessageListenerAdapter，将指定的处理器类与消息处理方法关联
        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(applicationContext.getBean(handlerClass), "handleMessage");
        // 设置消息确认模式为手动
        //messageListenerAdapter.containerAckMode(AcknowledgeMode.MANUAL);
        container.setMessageListener(messageListenerAdapter); // 设置消息监听器
        container.start(); // 启动监听容器
        containers.put(queueName, container); // 将监听容器与队列名称映射保存起来
    }

    public void unregisterListener(String queueName) {
        SimpleMessageListenerContainer container = containers.get(queueName);
        if (container != null) {
            container.stop();
            containers.remove(queueName);
        }
    }
}

