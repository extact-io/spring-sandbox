package sample.spring.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleNameHealthIndicator implements HealthIndicator {

    private final NameHolder nameHolder;

    public SimpleNameHealthIndicator(NameHolder nameHolder) {
        this.nameHolder = nameHolder;
    }

    @Override
    public Health health() {
        return doHealth();
    }

    private Health doHealth() {
        if (nameHolder.name().endsWith("1")) {
            log.info("★Simple1：invoke");
            return Health.up().withDetail("name", nameHolder).build();
        } else {
            log.info("★Simple2：invoke");
            return Health.down().withDetail("name", nameHolder).build();
        }
    }
}
