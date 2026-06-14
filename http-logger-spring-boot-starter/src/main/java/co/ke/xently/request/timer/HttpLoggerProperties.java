package co.ke.xently.request.timer;

import lombok.*;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.context.properties.ConfigurationProperties;

@NullMarked
@ToString
@Getter
@Setter
@Builder
@EqualsAndHashCode
@ConfigurationProperties(prefix = "log.http")
public class HttpLoggerProperties {
    @Builder.Default
    private boolean enabled = true;
}
