package com.playtika.test.rocketmq;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties("embedded.rocketmq.broker")
public class RocketMQBrokerProperties extends RocketMQProperties {
    public static final String BEAN_NAME_EMBEDDED_ROCKETMQ_BROKER = "embeddedRocketMQBroker";

    private int port = 10911;
}
