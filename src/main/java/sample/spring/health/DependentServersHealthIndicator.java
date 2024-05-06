package sample.spring.health;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.health.StatusAggregator;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import sample.spring.health.client.ProbeResult;
import sample.spring.health.client.ReadinessProbeRestClient;
import sample.spring.health.client.ReadinessProbeRestClientFactory;

@Slf4j
public class DependentServersHealthIndicator implements HealthIndicator {

    private ReadinessProbeRestClient client;
    private List<String> probeUrls;

    public DependentServersHealthIndicator(ReadinessProbeRestClientFactory factory, Properties properties) {
        this.client = factory.create();
        this.probeUrls = properties.getDepends();
    }

    @Override
    public Health health() {

        // createTask and execute async job
        List<ProbeTask> tasks = probeUrls.stream()
                .map(url -> new ProbeTask(url, client)) // execute async
                .toList();

        // convert to future array from task list
        @SuppressWarnings({ "unchecked" })
        CompletableFuture<ProbeResult>[] futureArray = tasks.stream()
                .map(ProbeTask::getFuture)
                .toArray(CompletableFuture[]::new);

        // join thread
        final var finalTasks = tasks; // for lambda ref.
        CompletableFuture<Void> promise = CompletableFuture.allOf(futureArray);
        List<ProbeResult> results = promise.thenApply(dummy -> {
            return finalTasks.stream()
                    .map(ProbeTask::getResult)
                    .toList();
        }).join();

        // build response
        Set<Status> status = results.stream().map(ProbeResult:: status).collect(Collectors.toSet());
        Status aggregatedStatus = StatusAggregator.getDefault().getAggregateStatus(status);
        Health.Builder builder = Health.status(aggregatedStatus);
        results.forEach(result -> {
            builder.withDetail(result.probeUrl(), result.status().getCode());
        });

        return builder.build();
    }

    // -------------------------------------------------------------- inner classes.

    @Getter
    static class ProbeTask {

        String probeUrl;
        ReadinessProbeRestClient client;
        CompletableFuture<ProbeResult> future;

        ProbeTask(String probeUrl, ReadinessProbeRestClient client) {
            this.probeUrl = probeUrl;
            this.client = client;
            this.future = client.probeReadinessAsync(probeUrl).toCompletableFuture();
        }

        ProbeResult getResult() {
            try {
                return future.join();
            } catch (Exception e) {
                log.warn("occur exception. probe.url=[" + probeUrl + "]", e);
                return new ProbeResult(probeUrl, Status.DOWN);
            }
        }
    }

    @ConfigurationProperties(prefix = "sandbox.health")
    @Getter
    @Setter
    static class Properties {
        List<String> depends;
    }
}
