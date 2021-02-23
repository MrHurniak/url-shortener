package url.shortener.server.config.exception;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotUniqueIdException extends RuntimeException {

  private List<String> availableIds;
}
