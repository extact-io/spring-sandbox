package sample.spring.health;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.actuate.health.CompositeHealthContributor;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;
import sample.spring.async.AsyncInvoker;
import sample.spring.condition.ConditionalOnPropertyList;
import sample.spring.health.SimpleCompositeHealthConfiguration.NameHoldersProperties;
import sample.spring.health.client.ReadinessProbeRestClientFactory;
import sample.spring.health.client.ReadinessProbeRestClientFactoryImpl;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ NameHoldersProperties.class, DependentServersHealthIndicator.Properties.class })
public class SimpleCompositeHealthConfiguration {

    @Bean
    HealthContributor simpleCompositeHealthContributor(NameHoldersProperties properties) {
        return createContributor(properties.nameHolderMap());
    }

    @Bean
    AsyncInvoker asyncInvoker() {
        return new AsyncInvoker();
    }

    @Bean
    @ConditionalOnPropertyList(key = "sandbox.health.depends")
    DependentServersHealthIndicator dependentServersHealthIndicator(AsyncInvoker asyncInvoker, DependentServersHealthIndicator.Properties properties) {
        ReadinessProbeRestClientFactory factory = new ReadinessProbeRestClientFactoryImpl(asyncInvoker);
        return new DependentServersHealthIndicator(factory, properties);
    }

    @Getter
    @Setter
    @ConfigurationProperties(prefix = "sandbox.health")
    static class NameHoldersProperties {

        private List<String> names = new ArrayList<>();

        Map<String, NameHolder> nameHolderMap() {
            return names.stream()
                    .collect(Collectors.toMap(name -> name, NameHolder::new));
        }
    }

    private HealthContributor createContributor(Map<String, NameHolder> beans) {
        if (beans.size() == 1) {
            return createContributor(beans.values().iterator().next());
        }
        return CompositeHealthContributor.fromMap(beans, this::createContributor);
    }

    private HealthContributor createContributor(NameHolder nameHolder) {
        return new SimpleNameHealthIndicator(nameHolder);
    }
}
