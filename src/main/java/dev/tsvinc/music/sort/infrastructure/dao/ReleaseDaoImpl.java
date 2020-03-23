package dev.tsvinc.music.sort.infrastructure.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import dev.tsvinc.music.sort.infrastructure.domain.Release;
import java.sql.SQLException;
import java.util.List;

public class ReleaseDaoImpl extends BaseDaoImpl<Release, Long> implements ReleaseDao {

  protected ReleaseDaoImpl(Class<Release> dataClass) throws SQLException {
    super(dataClass);
  }

  protected ReleaseDaoImpl(ConnectionSource connectionSource, Class<Release> dataClass)
      throws SQLException {
    super(connectionSource, dataClass);
  }

  protected ReleaseDaoImpl(ConnectionSource connectionSource, DatabaseTableConfig<Release> tableConfig)
      throws SQLException {
    super(connectionSource, tableConfig);
  }

  @Override
  public List<Release> findByReleaseName(String name) throws SQLException {
    return super.queryForEq("release_name", name);
  }
}
