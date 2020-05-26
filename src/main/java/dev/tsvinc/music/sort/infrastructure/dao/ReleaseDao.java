package dev.tsvinc.music.sort.infrastructure.dao;

import com.j256.ormlite.dao.Dao;
import dev.tsvinc.music.sort.infrastructure.domain.Release;

import java.sql.SQLException;
import java.util.List;

public interface ReleaseDao extends Dao<Release, Long> {
  List<Release> findByReleaseName(String name) throws SQLException;
}
