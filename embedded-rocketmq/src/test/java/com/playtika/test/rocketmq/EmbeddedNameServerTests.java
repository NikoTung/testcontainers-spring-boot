package com.playtika.test.rocketmq;

import com.playtika.test.rocketmq.configuration.RocketMQNameServerContainerConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.PullStatus;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = EmbeddedNameServerTests.TestConfiguration.class,
        properties = {"embedded.rocketmq.enabled=true",}
)
@ActiveProfiles("enabled")
public class EmbeddedNameServerTests {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private Environment environment;



    @Test
    public void dockerEnvironmentShouldRegistered() {
        String nameServer = environment.getProperty(RocketMQNameServerContainerConfiguration.EMBEDDED_NAME_SERVER_CONNECT);
        String nameServerHost = environment.getProperty(RocketMQNameServerContainerConfiguration.EMBEDDED_NAME_SERVER_CONTAINER_CONNECT);
        assertThat(nameServer).isNotBlank();
        assertThat(nameServerHost).isNotBlank();
    }

    @Test
    public void messageShouldSendAndReceived() throws InterruptedException, RemotingException, MQClientException, MQBrokerException {

        Message<String> msg = MessageBuilder.
                withPayload("This echo : " + System.currentTimeMillis())
                .build();
        SendResult sendResult = rocketMQTemplate.syncSend("test-topic", msg);
        assertThat(sendResult.getSendStatus()).isEqualTo(SendStatus.SEND_OK);

        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer("test-Group");
        consumer.setNamesrvAddr(environment.getProperty(RocketMQNameServerContainerConfiguration.EMBEDDED_NAME_SERVER_CONNECT));
        consumer.start();
        PullResult pull = consumer.pull(sendResult.getMessageQueue(), "*", 0L, 1);
        PullStatus pullStatus = pull.getPullStatus();
        assertThat(pullStatus).isEqualByComparingTo(PullStatus.FOUND);

    }

    @EnableAutoConfiguration
    @Configuration
    static class TestConfiguration {
    }
}
