package com.playtika.test.rocketmq;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties("embedded.rocketmq")
public class RocketMQNameServerProperties extends RocketMQProperties {
    public static final String BEAN_NAME_EMBEDDED_ROCKETMQ_NAMESERVER = "embeddedRocketMQNameServer";
    private int port = 9876;
}
