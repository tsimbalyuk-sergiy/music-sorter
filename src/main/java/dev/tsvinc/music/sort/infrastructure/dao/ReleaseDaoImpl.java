package dev.tsvinc.music.sort.infrastructure.dao;

import static org.dizitart.no2.objects.filters.ObjectFilters.eq;

import dev.tsvinc.music.sort.infrastructure.domain.Release;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;

public class ReleaseDaoImpl implements ReleaseDao {

  @Inject
  private NitriteInstanceImp nitrite;

  @Override
  public Optional<Release> findByReleaseName(String name) {
    return Optional.ofNullable(getNitriteRepository().find(eq("release_name", name)).firstOrDefault());
  }

  @Override
  public List<Release> findAll() {
    return getNitriteRepository().find(ObjectFilters.ALL).toList();
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

  private ObjectRepository<Release> getNitriteRepository() {
    return nitrite.instance().getRepository(Release.class);
  }
}
