package sample.spring.condition;

import java.util.function.Supplier;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionMessage.Builder;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.OnPropertyListCondition;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnPropertyListConditionAnnotationAware extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String key = (String) metadata.getAnnotationAttributes(ConditionalOnPropertyList.class.getName()).get("key");
        SpringBootCondition delegateCondition = new OnPropertyListConditionProxy(key,
                () -> ConditionMessage.forCondition(key));
        return delegateCondition.getMatchOutcome(context, metadata);
    }

    static class OnPropertyListConditionProxy extends OnPropertyListCondition {
        protected OnPropertyListConditionProxy(String propertyName, Supplier<Builder> messageBuilder) {
            super(propertyName, messageBuilder);
        }
    }
}
