package url.shortener.server.config.properties;

import io.micronaut.context.annotation.ConfigurationProperties;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
@ConfigurationProperties("service")
public class ServiceProperties {

  @NotBlank
  private String host;

  @NotBlank
  private String schema;

}
