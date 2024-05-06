package sample.spring.async;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.springframework.scheduling.annotation.Async;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncInvoker {

    @Async
    public <T> CompletableFuture<T> invoke(Supplier<T> supplier) {
        log.debug("run asynce...-> {}", supplier);
        return CompletableFuture.completedFuture(supplier.get());
    }

    @Async
    public CompletableFuture<Void> invoke(Runnable task) {
        log.debug("run asynce...-> {}", task);
        return CompletableFuture.runAsync(task);
    }
}
