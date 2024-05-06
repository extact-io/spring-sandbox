package sample.spring.health.client;

import java.util.concurrent.CompletionStage;

import org.springframework.boot.actuate.health.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;
import sample.spring.async.AsyncInvoker;

@Slf4j
public class ReadinessProbeRestClientImpl implements ReadinessProbeRestClient {

    private AsyncInvoker asyncInvoker;
    private RestClient restClient;
    private StatusHttpCodeMapper statusMapper;

    ReadinessProbeRestClientImpl(AsyncInvoker asyncInvoker, RestClient restClient, StatusHttpCodeMapper mapper) {
        this.asyncInvoker = asyncInvoker;
        this.restClient = restClient;
        this.statusMapper = mapper;
    }

    @Override
    public CompletionStage<ProbeResult> probeReadinessAsync(String url) {
        return asyncInvoker.invoke(() -> this.probeReadiness(url));
    }

    @Override
    public ProbeResult probeReadiness(String url) {

        try {
            ResponseEntity<String> result = restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .toEntity(String.class);

            Status healtStatus = statusMapper.resolve(result.getStatusCode().value());
            if (healtStatus == Status.UNKNOWN) {
                log.warn("unknown status code was returned from {}. statuscode={}", url, result.getStatusCode().value());
            }

            return new ProbeResult(url, healtStatus);

        } catch (Exception e) {
            log.warn("exception occurred at readiness probe. probeUrl=%s".formatted(url), e);
            return new ProbeResult(url, Status.DOWN);

        }
    }
}
