package dev.tsvinc.music.sort.infrastructure.nosql.dao;

import dev.tsvinc.music.sort.infrastructure.domain.Release;
import java.util.List;

public interface ReleaseDao {
  List<Release> findByReleaseName(String name);

  List<Release> findAll();

  Release findOne(String releaseName);

  Release update(Release release);

  boolean delete(Release release);

  Release save(Release release);
}
