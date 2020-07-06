package dev.tsvinc.music.sort.infrastructure.nosql.dao;

import dev.tsvinc.music.sort.infrastructure.domain.Release;
import java.util.List;

public class ReleaseDaoImpl implements ReleaseDao {

  @Override
  public List<Release> findByReleaseName(
      String name) {
    return null;
  }

  @Override
  public List<Release> findAll() {
    return null;
  }

  @Override
  public Release findOne(String releaseName) {
    return null;
  }

  @Override
  public Release update(Release release) {
    return null;
  }

  @Override
  public boolean delete(Release release) {
    return false;
  }

  @Override
  public Release save(Release release) {
    return null;
  }
}
