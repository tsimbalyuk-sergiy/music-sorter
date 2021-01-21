package dev.tsvinc.music.sort.infrastructure.dao;

import static org.dizitart.no2.objects.filters.ObjectFilters.eq;

import dev.tsvinc.music.sort.infrastructure.domain.Release;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.dizitart.no2.util.Iterables;
import org.pmw.tinylog.Logger;

public class ReleaseDaoImpl implements ReleaseDao {

  @Inject private NitriteInstanceImp nitrite;

  @Override
  public Optional<Release> findByReleaseName(final String name) {
    return Optional.ofNullable(
        this.getNitriteRepository().find(eq("release_name", name)).firstOrDefault());
  }

  @Override
  public List<Release> findAll() {
    return this.getNitriteRepository().find(ObjectFilters.ALL).toList();
  }

  @Override
  public Release findOne(final String releaseName) {
    return null;
  }

  @Override
  public Release update(final Release release) {
    Logger.info("update TBD");
    return null;
  }

  @Override
  public boolean delete(final Release release) {
    Logger.info("delete TBD");
    return false;
  }

  @Override
  public Number save(final Release release) {
    return Iterables.firstOrDefault(this.getNitriteRepository().insert(release)).getIdValue();
  }

  private ObjectRepository<Release> getNitriteRepository() {
    return this.nitrite.instance().getRepository(Release.class);
  }
}
