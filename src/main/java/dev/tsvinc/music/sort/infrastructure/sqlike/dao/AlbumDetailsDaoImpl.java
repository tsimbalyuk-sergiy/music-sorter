package dev.tsvinc.music.sort.infrastructure.sqlike.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import dev.tsvinc.music.sort.infrastructure.sqlike.domain.AlbumDetails;

import java.sql.SQLException;

public class AlbumDetailsDaoImpl extends BaseDaoImpl<AlbumDetails, Long>
    implements AlbumDetailsDao {

  protected AlbumDetailsDaoImpl(Class<AlbumDetails> dataClass) throws SQLException {
    super(dataClass);
  }

  protected AlbumDetailsDaoImpl(ConnectionSource connectionSource, Class<AlbumDetails> dataClass)
      throws SQLException {
    super(connectionSource, dataClass);
  }

  protected AlbumDetailsDaoImpl(
      ConnectionSource connectionSource, DatabaseTableConfig<AlbumDetails> tableConfig)
      throws SQLException {
    super(connectionSource, tableConfig);
  }
}
