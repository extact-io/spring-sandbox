package sample.spring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Component
@ConditionalOnProperty(name = "sandbox.service.hello", havingValue = "jpa")
@Transactional
public class HelloServiceByJpa implements HelloService {

    @PersistenceContext(unitName = "test1")
    private EntityManager em;

    @Override
    public String hello() {
        return em.find(Message.class, 1L).getMessage();
    }
}
