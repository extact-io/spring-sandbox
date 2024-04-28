package sample.spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "sandbox.service.hello", havingValue = "config")
public class HelloServiceByConfig implements HelloService {

    @Value("${config.val}")
    private String configValue;

    @Override
    public String hello() {
        return configValue;
    }
}
