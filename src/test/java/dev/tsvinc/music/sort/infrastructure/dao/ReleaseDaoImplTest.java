package dev.tsvinc.music.sort.infrastructure.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.TableUtils;
import dev.tsvinc.music.sort.infrastructure.domain.AlbumDetails;
import dev.tsvinc.music.sort.infrastructure.domain.Format;
import dev.tsvinc.music.sort.infrastructure.domain.Genre;
import dev.tsvinc.music.sort.infrastructure.domain.Release;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class ReleaseDaoImplTest {

  public static final String NEW_RELEASE = "new release";
  private static JdbcPooledConnectionSource connectionSource;

  private static Dao<Release, Long> releases;
  private static Dao<AlbumDetails, Long> albumDetails;

  @BeforeAll
  public static void setup() throws SQLException {
    connectionSource = new JdbcPooledConnectionSource("jdbc:sqlite:music-sorter.db");

    TableUtils.createTableIfNotExists(connectionSource, AlbumDetails.class);
    TableUtils.createTableIfNotExists(connectionSource, Release.class);

    releases = DaoManager.createDao(connectionSource, Release.class);
    albumDetails = DaoManager.createDao(connectionSource, AlbumDetails.class);

    GenericRawResults<String[]> results =
        releases.queryRaw("SELECT name FROM sqlite_master WHERE type = 'table'");
    for (String[] result : results) {
      System.out.println("One table is: " + result[0]);
    }
  }

  @Test
  void findByName() throws SQLException {
    final var genre = Genre.builder().value("some").build();
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
    assertEquals(NEW_RELEASE, result.getReleaseName());

    release.setReleaseName("my other release");
    releases.update(release);
    releases.queryForAll().forEach(rls -> System.out.println(">>> " + rls.toString()));
    releases.delete(release);
  }

  @AfterAll
  public void clear() throws SQLException, IOException {
    TableUtils.clearTable(connectionSource, Release.class);
    TableUtils.clearTable(connectionSource, AlbumDetails.class);
    connectionSource.close();

    boolean delete = Files.deleteIfExists(Paths.get("music-sorter.db"));
    System.out.println("Deleted db file: " + delete);
  }
}
