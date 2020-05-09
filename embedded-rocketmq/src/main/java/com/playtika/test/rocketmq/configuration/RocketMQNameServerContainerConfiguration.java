package com.playtika.test.rocketmq.configuration;

import com.github.dockerjava.api.model.Capability;
import com.playtika.test.rocketmq.RocketMQNameServerProperties;
import lombok.extern.slf4j.Slf4j;
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
import static com.playtika.test.rocketmq.RocketMQNameServerProperties.BEAN_NAME_EMBEDDED_ROCKETMQ_NAMESERVER;

@Slf4j
@Configuration
@ConditionalOnProperty(value = "embedded.rocketmq.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RocketMQNameServerProperties.class)
public class RocketMQNameServerContainerConfiguration {

    private static final String NAMESERVER_HOST_NAME = "rocketmq.nameserver";
    public static final String EMBEDDED_NAME_SERVER_CONTAINER_CONNECT = "embedded.rocketmq.nameServer.containerConnect";

    public static final String EMBEDDED_NAME_SERVER_CONNECT = "embedded.rockectmq.nameserver";

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(Network.class)
    public Network network() {
        Network network = Network.newNetwork();
        log.info("Created docker Network id={}", network.getId());
        return network;
    }

    @SuppressWarnings("rawtypes")
    @Bean(name = BEAN_NAME_EMBEDDED_ROCKETMQ_NAMESERVER, destroyMethod = "stop")
    public GenericContainer nameServer(
            RocketMQNameServerProperties nameServerProperties,
            ConfigurableEnvironment environment,
            Network network) {
        log.info("Starting name server. Docker image: {}", nameServerProperties.getDockerImage());

        GenericContainer nameServer =
                new GenericContainer<>(nameServerProperties.getDockerImage())
                        .withLogConsumer(containerLogsConsumer(log))
                        .withExposedPorts(nameServerProperties.getPort())
                        .withCreateContainerCmdModifier(cmd -> cmd.withHostName(NAMESERVER_HOST_NAME))
                        .withCreateContainerCmdModifier(cmd -> cmd.withCapAdd(Capability.NET_ADMIN))
//                        .waitingFor(redisStatusCheck)
                        .withNetwork(network)
                        .withCommand("sh", "mqnamesrv")
                        .withNetworkAliases(NAMESERVER_HOST_NAME)
                        .withStartupTimeout(nameServerProperties.getTimeoutDuration());


        nameServer.start();
        registerNameServerEnvironment(nameServer, environment, nameServerProperties);
        return nameServer;
    }

    @SuppressWarnings("rawtypes")
    private void registerNameServerEnvironment(GenericContainer nameServer,
                                               ConfigurableEnvironment environment,
                                               RocketMQNameServerProperties nameServerProperties) {

        Integer port = nameServer.getMappedPort(nameServerProperties.getPort());
        String ipAddress = nameServer.getContainerIpAddress();

        LinkedHashMap<String, Object> map = new LinkedHashMap<>();

        String nameServerConnectForContainers = String.format("%s:%d", NAMESERVER_HOST_NAME, nameServerProperties.getPort());
        map.put(EMBEDDED_NAME_SERVER_CONTAINER_CONNECT, nameServerConnectForContainers);

        String nameServerConnectForClient = String.format("%s:%d", ipAddress, port);

        map.put(EMBEDDED_NAME_SERVER_CONNECT, nameServerConnectForClient);

        MapPropertySource propertySource = new MapPropertySource("embeddedNameServerInfo", map);
        environment.getPropertySources().addFirst(propertySource);
    }
}
