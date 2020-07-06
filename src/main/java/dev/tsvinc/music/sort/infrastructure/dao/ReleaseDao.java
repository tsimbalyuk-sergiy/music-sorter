package dev.tsvinc.music.sort.infrastructure.dao;

import dev.tsvinc.music.sort.infrastructure.domain.Release;
import java.util.List;
import java.util.Optional;

public interface ReleaseDao {

  Optional<Release> findByReleaseName(String name);

  List<Release> findAll();

  Release findOne(String releaseName);

  Release update(Release release);

  boolean delete(Release release);

  Number save(Release release);
}
