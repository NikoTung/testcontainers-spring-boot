package com.playtika.test.rocketmq.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import javax.annotation.PostConstruct;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Slf4j
@Configuration
@Order(HIGHEST_PRECEDENCE)
@Import({RocketMQNameServerContainerConfiguration.class, RocketMQBrokerContainerConfiguration.class})
public class EmbeddedRocketMQBootstrapConfiguration {
}
