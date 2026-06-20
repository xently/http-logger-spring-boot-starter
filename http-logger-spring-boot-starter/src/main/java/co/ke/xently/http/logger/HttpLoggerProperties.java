package co.ke.xently.http.logger;

import lombok.*;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.context.properties.ConfigurationProperties;

@NullMarked
@ToString
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "log.http")
public final class HttpLoggerProperties {
    private boolean enabled = false;
}
