package sample.spring.health.client;

import java.util.concurrent.CompletionStage;

public interface ReadinessProbeRestClient {

    ProbeResult probeReadiness(String url);

    CompletionStage<ProbeResult> probeReadinessAsync(String url);
}
