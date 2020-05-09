package com.playtika.test.rocketmq.configuration;

import com.github.dockerjava.api.model.Capability;
import com.playtika.test.rocketmq.RocketMQBrokerProperties;
import com.playtika.test.rocketmq.RocketMQNameServerProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import java.util.LinkedHashMap;

import static com.playtika.test.common.utils.ContainerUtils.containerLogsConsumer;
import static com.playtika.test.rocketmq.RocketMQBrokerProperties.BEAN_NAME_EMBEDDED_ROCKETMQ_BROKER;
import static com.playtika.test.rocketmq.RocketMQNameServerProperties.BEAN_NAME_EMBEDDED_ROCKETMQ_NAMESERVER;

@Slf4j
@Configuration
@ConditionalOnProperty(value = "embedded.rocketmq.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RocketMQBrokerProperties.class)
@ConditionalOnBean(RocketMQNameServerContainerConfiguration.class)
public class RocketMQBrokerContainerConfiguration {

    public static final String BROKER_HOST_NAME = "broker-a";

    @SuppressWarnings("rawtypes")
    @Bean(name = BEAN_NAME_EMBEDDED_ROCKETMQ_BROKER, destroyMethod = "stop")
    public GenericContainer nameServer(@Qualifier(BEAN_NAME_EMBEDDED_ROCKETMQ_NAMESERVER) GenericContainer nameServer,
            RocketMQBrokerProperties brokerProperties,
                                      ConfigurableEnvironment environment,
                                      Network network) {
        log.info("Starting broker. Docker image: {}", brokerProperties.getDockerImage());

        GenericContainer broker =
                new GenericContainer<>(brokerProperties.getDockerImage())
                        .withLogConsumer(containerLogsConsumer(log))
                        .withExposedPorts(brokerProperties.getPort(), 10909, 10912)
                        .withEnv("NAMESRV_ADDR", environment.getProperty(RocketMQNameServerContainerConfiguration.EMBEDDED_NAME_SERVER_CONTAINER_CONNECT))
                        .withCreateContainerCmdModifier(cmd -> cmd.withHostName(BROKER_HOST_NAME))
                        .withCreateContainerCmdModifier(cmd -> cmd.withCapAdd(Capability.NET_ADMIN))
//                        .waitingFor(redisStatusCheck)
                        .withNetwork(network)
                        .dependsOn(nameServer)
                        .withCommand("sh", "mqbroker")
                        .withStartupTimeout(brokerProperties.getTimeoutDuration());


        broker.start();
        return broker;
    }


}
