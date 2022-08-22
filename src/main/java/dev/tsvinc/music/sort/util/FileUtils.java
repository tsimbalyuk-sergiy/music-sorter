package dev.tsvinc.music.sort.util;

import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static dev.tsvinc.music.sort.Constants.FLAC_EXT;
import static dev.tsvinc.music.sort.Constants.MP3_EXT;

@SuppressWarnings("unused")
public class FileUtils {
  //  private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

  private FileUtils() {}

  private static List<String> liveReleasesPatterns;

  private static void find(final String folder, final String matcher) {
    try (final var find =
        Files.find(
            Paths.get(folder),
            Integer.MAX_VALUE,
            isDirAndContainsPatternInNameBiPredicate(matcher))) {
      final var collect = find.collect(Collectors.toList());
      if (!collect.isEmpty()) {
        Logger.info("collected: {}", collect);
      } else {
        Logger.info("no matches found");
      }
    } catch (final IOException e) {
      Logger.error("error: {}", e.getMessage(), e);
    }
  }

  public static BiPredicate<Path, BasicFileAttributes> isDirAndContainsPatternInNameBiPredicate(
      final String pattern) {
    return (filePath, fileAttr) ->
        fileAttr.isDirectory() && filePath.toFile().getName().contains(pattern);
  }

  public static boolean isDirAndContainsPatternInName(final Path path) {
    return (path.toFile().isDirectory() && containsPatternFromList(path.getParent().toString()));
  }

  public static boolean containsPatternFromList(String path) {
    AtomicBoolean result = new AtomicBoolean(false);
    getLiveReleasesPatterns()
        .forEach(
            o -> {
              if (path.contains(o)) result.set(true);
            });
    return result.getPlain();
  }

  public static boolean isAudioFile(final Path path) {
    return path.getFileName().toString().contains(MP3_EXT)
        || path.getFileName().toString().contains(FLAC_EXT);
  }

  public static boolean isMp3(final Path path) {
    return path.getFileName().toString().contains(MP3_EXT);
  }

  public static boolean isFlac(final Path path) {
    return path.getFileName().toString().contains(FLAC_EXT);
  }

  public static List<String> getLiveReleasesPatterns() {
    return liveReleasesPatterns;
  }

  public static void setLiveReleasesPatterns(List<String> liveReleasesPatterns) {
    Logger.debug("live releases patterns: {}", liveReleasesPatterns.toString());
    FileUtils.liveReleasesPatterns = liveReleasesPatterns;
  }

  public static boolean isEmptyDirectory(final Path directory) {
    final var obj = directory.toFile().listFiles();
    if (obj != null) {
      return obj.length == 0;
    } else {
      return false;
    }
  }
}
