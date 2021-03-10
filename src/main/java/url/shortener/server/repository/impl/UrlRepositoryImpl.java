package url.shortener.server.repository.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import url.shortener.server.bigtable.BigTable;
import url.shortener.server.entity.ShortenedUrl;
import url.shortener.server.repository.UrlRepository;

@Slf4j
@Singleton
public class UrlRepositoryImpl implements UrlRepository {

  private static final int AVAILABLE_KEY_TRIES_MAX_ATTEMPTS = 20;

  private final BigTable urlTable;
  private final int maxKeyLength;

  public UrlRepositoryImpl(@Named("urlTable") BigTable urlTable) {
    this.urlTable = urlTable;
    this.maxKeyLength = 10;
  }


  @Override
  public List<ShortenedUrl> findAll(@NotNull Set<String> aliases) {
    return aliases
        .stream()
        .parallel()
        .map(alias -> urlTable.findByKey(alias).map(value -> toUrl(alias, value)))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  private ShortenedUrl toUrl(String alias, String value) {
    return new ShortenedUrl()
        .setAlias(alias)
        .setOriginalUrl(URI.create(value));
  }

  @Override
  public Optional<ShortenedUrl> findById(@NotNull String alias) {
    return urlTable.findByKey(alias)
        .map(value -> toUrl(alias, value));
  }

  @Override
  public boolean save(@NotNull ShortenedUrl shortenedUrl) {
    return urlTable.put(shortenedUrl.getAlias(), shortenedUrl.getOriginalUrl().toString());
  }

  @Override
  public void deleteById(@NotNull String alias) {
    urlTable.deleteByKey(alias);
  }

  @Override
  public boolean existsById(@NotNull String alias) {
    return urlTable.containsKey(alias);
  }

  @Override
  public String nextAvailableAlias() {
    for (int i = 0; i < AVAILABLE_KEY_TRIES_MAX_ATTEMPTS; i++) {
      String possibleKey = RandomStringUtils.randomAlphanumeric(1, maxKeyLength);
      if (!urlTable.containsKey(possibleKey)) {
        return possibleKey;
      }
    }
    throw new IllegalStateException("Cannot find available alias");
  }

  @Override
  public List<String> findAvailableAliases(int count) {
    List<String> availableAliases = new ArrayList<>(count);

    for (int i = 0; i < count; i++) {
      try {
        availableAliases.add(nextAvailableAlias());
      } catch (Exception e) {
        log.error("Could not find available alias");
      }
    }
    return availableAliases;
  }
}
