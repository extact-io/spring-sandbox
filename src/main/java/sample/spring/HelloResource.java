package sample.spring;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloResource {

    private HelloService service;

    public HelloResource(HelloService service) {
        this.service = service;
    }

    @GetMapping("/hello")
    public String hello() {
        return service.hello();
    }
}
