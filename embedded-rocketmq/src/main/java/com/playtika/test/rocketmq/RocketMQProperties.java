package com.playtika.test.rocketmq;

import com.playtika.test.common.properties.CommonContainerProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RocketMQProperties extends CommonContainerProperties {
    private String dockerImage = "apacherocketmq/rocketmq:4.6.0";
}
