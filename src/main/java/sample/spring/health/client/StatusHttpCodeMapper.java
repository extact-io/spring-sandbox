package sample.spring.health.client;

import java.util.Map;

import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;

public interface StatusHttpCodeMapper {

    Status resolve(int httpStatusCode);

    static StatusHttpCodeMapper defaultMapping() {
        return DefaultStatusCodeMapper.INSTANCE;
    }

    static class DefaultStatusCodeMapper implements StatusHttpCodeMapper {

        private static final StatusHttpCodeMapper INSTANCE = new DefaultStatusCodeMapper();

        private static final Map<Integer, Status> MAPPINGS = Map.of(
                HttpStatus.OK.value(), Status.UP,
                HttpStatus.SERVICE_UNAVAILABLE.value(), Status.DOWN,
                HttpStatus.INTERNAL_SERVER_ERROR.value(), Status.DOWN
            );

        @Override
        public Status resolve(int httpStatusCode) {
            return MAPPINGS.getOrDefault(httpStatusCode, Status.UNKNOWN);
        }
    }
}
