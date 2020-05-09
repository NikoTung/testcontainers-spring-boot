package com.playtika.test.rocketmq;


import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = DisableRocketMQTests.TestConfiguration.class
)
@ActiveProfiles("disabled")
public class DisableRocketMQTests {

    @Autowired
    ConfigurableListableBeanFactory beanFactory;
    @Test
    public void beanShouldNotFound() {
        String[] containers = beanFactory.getBeanNamesForType(GenericContainer.class);

        assertThat(containers).isEmpty();

        assertThatThrownBy(() -> beanFactory.getBean(RocketMQBrokerProperties.BEAN_NAME_EMBEDDED_ROCKETMQ_BROKER))
                .isInstanceOf(BeansException.class);
        assertThatThrownBy(() -> beanFactory.getBean(RocketMQNameServerProperties.BEAN_NAME_EMBEDDED_ROCKETMQ_NAMESERVER))
                .isInstanceOf(BeansException.class);
    }

    @EnableAutoConfiguration
    @Configuration
    static class TestConfiguration {
    }
}
