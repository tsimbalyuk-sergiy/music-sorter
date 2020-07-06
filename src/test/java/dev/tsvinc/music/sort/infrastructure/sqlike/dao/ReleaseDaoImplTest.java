package dev.tsvinc.music.sort.infrastructure.sqlike.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.TableUtils;
import dev.tsvinc.music.sort.infrastructure.sqlike.domain.AlbumDetails;
import dev.tsvinc.music.sort.infrastructure.sqlike.domain.Format;
import dev.tsvinc.music.sort.infrastructure.sqlike.domain.Release;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.pmw.tinylog.Logger;

@TestInstance(Lifecycle.PER_CLASS)
class ReleaseDaoImplTest {

  public static final String NEW_RELEASE = "new release";
  private static JdbcPooledConnectionSource connectionSource;

  private static Dao<Release, Long> releases;

  @BeforeAll
  public static void setup() throws SQLException {
    connectionSource = new JdbcPooledConnectionSource("jdbc:sqlite:music-sorter.db");

    TableUtils.createTableIfNotExists(connectionSource, AlbumDetails.class);
    TableUtils.createTableIfNotExists(connectionSource, Release.class);

    releases = DaoManager.createDao(connectionSource, Release.class);

    GenericRawResults<String[]> results =
        releases.queryRaw("SELECT name FROM sqlite_master WHERE type = 'table'");
    for (String[] result : results) {
      Logger.info("One table is: " + result[0]);
    }
  }

  @Test
  void findByName() throws SQLException {
    Release release =
        Release.builder()
            .releaseName(NEW_RELEASE)
            .fullPath("path")
            .releaseSize(10000L)
            .format(Format.FLAC)
            .numberOfAudioFiles(10)
            .hasNfo(false)
            .hasChecksum(false)
            .checksumValid(false)
            .albumDetails(
                AlbumDetails.builder()
                    .albumArtist("album_artist")
                    .artist("artist")
                    .genre("some")
                    .year(2000)
                    .build())
            .build();
    releases.create(release);

    Release result = releases.queryForId(release.getId());
    Logger.info(">>> " + result.toString());
    assertEquals(NEW_RELEASE, result.getReleaseName());

    release.setReleaseName("my other release");
    releases.update(release);
    final Release updatedRelease = releases.queryForId(release.getId());
    assertEquals("my other release", updatedRelease.getReleaseName());
    releases.delete(release);
  }

  @AfterAll
  public void clear() throws SQLException, IOException {
    TableUtils.clearTable(connectionSource, Release.class);
    TableUtils.clearTable(connectionSource, AlbumDetails.class);
    connectionSource.close();

    boolean delete = Files.deleteIfExists(Paths.get("music-sorter.db"));
    Logger.info("Deleted db file: " + delete);
  }
}
