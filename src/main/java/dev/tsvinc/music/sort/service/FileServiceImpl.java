package dev.tsvinc.music.sort.service;

import static dev.tsvinc.music.sort.util.Constants.CHECKSUM;
import static dev.tsvinc.music.sort.util.Constants.FLAC;
import static dev.tsvinc.music.sort.util.Constants.FLAC_FORMAT;
import static dev.tsvinc.music.sort.util.Constants.MP3;
import static dev.tsvinc.music.sort.util.Constants.MP_3_FORMAT;
import static dev.tsvinc.music.sort.util.Predicates.IS_MUSIC_FILE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.pmw.tinylog.Logger.error;
import static org.pmw.tinylog.Logger.info;

import dev.tsvinc.music.sort.domain.AppProperties;
import dev.tsvinc.music.sort.domain.ListingWithFormat;
import dev.tsvinc.music.sort.infrastructure.domain.Release;
import io.vavr.control.Try;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.pmw.tinylog.Logger;

public class FileServiceImpl implements FileService {

  @Inject private PropertiesService propertiesService;
  @Inject private CleanUpService cleanUpService;
  @Inject private AudioFileService audioFileService;
  //  @Inject private DbService dbService;

  private static boolean isNotLiveRelease(String folderName, AppProperties properties) {
    return properties.getLiveReleasesPatterns().parallelStream().noneMatch(folderName::contains);
  }

  private static ProgressBar buildProgressBar(Set<String> folderList) {
    return new ProgressBarBuilder()
        .setInitialMax(folderList.size())
        .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BLOCK)
        .setTaskName("Working ...")
        .showSpeed()
        .build();
  }

  private static void moveDirectory(String sourceDirectory, File source, File destination) {
    Try.withResources(() -> Files.newDirectoryStream(Paths.get(sourceDirectory)))
        .of(
            directoryStream -> {
              directoryStream.forEach(
                  src -> move(src, destination.toPath().resolve(src.getFileName())));
              Files.deleteIfExists(source.toPath());
              return null;
            })
        .onFailure(
            throwable ->
                error(
                    "Failed to move directory: {}, {}",
                    sourceDirectory,
                    throwable.getMessage(),
                    throwable));
  }

  private static void move(Path src, Path dest) {
    Try.of(() -> Files.move(src, dest, REPLACE_EXISTING))
        .onFailure(
            throwable ->
                error(
                    "Failed to move file {} to {}, {}",
                    src,
                    dest,
                    throwable.getMessage(),
                    throwable));
  }

  private static void createDirectory(final File destination) {
    Try.of(() -> Files.createDirectories(destination.toPath()))
        .onFailure(e -> error("Failed to create a directory: {}", e.getMessage(), e));
  }

  private static void listFiles(
      final String folderName, final List<? super String> result, final String glob) {
    try (final var stream = Files.newDirectoryStream(Paths.get(folderName), glob)) {
      stream.forEach(o -> result.add(folderName + File.separator + o.getFileName()));
    } catch (final IOException e) {
      Logger.error(
          "Error listing directory: {} with a filter: {}, {}", folderName, FLAC, e.getMessage(), e);
    }
  }

  private boolean tryWithProgressBar(Set<String> folderList, AppProperties properties) {
    return Try.withResources(() -> buildProgressBar(folderList))
        .of(
            progressBar -> {
              folderList.forEach(
                  release -> {
                    moveRelease(release, properties);
                    progressBar.step();
                  });
              return true;
            })
        .onSuccess(status -> info("finished moving releases: {}", status))
        .onFailure(
            throwable -> error("had errors moving releases: {}", throwable.getMessage(), throwable))
        .get();
  }

  private void moveRelease(final String sourceDirectory, AppProperties properties) {
    final var source = new File(sourceDirectory);
    final var metadata = audioFileService.getMetadata(sourceDirectory);
    boolean containsCheckSum = false;
    if (hasCheckSum(source).containsKey(CHECKSUM)) {
      containsCheckSum = true;
      verifyCheckSum(source); // TODO
    }

    /*private String releaseName;
    private long releaseSize;
    private boolean hasNfo;
    private boolean hasChecksum;
    private boolean checksumValid;*/

    Release.builder()
        .artist(metadata.getArtist())
        .releaseName(source.getName())
        .hasChecksum(containsCheckSum)
        .build();

    final var outWithFormat = properties.getTargetFolder() + File.separator + metadata.getFormat();
    final var outWithFormatDir = new File(outWithFormat);
    if (!outWithFormatDir.exists()) {
      createDirectory(outWithFormatDir);
    }
    String outWithGenreAndFormat;
    if (properties.isSortByArtist()) {
      outWithGenreAndFormat =
          Path.of(outWithFormat, metadata.getGenre(), metadata.getArtist().trim()).toString();
    } else {
      outWithGenreAndFormat = Path.of(outWithFormat, metadata.getGenre()).toString();
    }
    final var genreDir = new File(outWithGenreAndFormat);
    final var finalDestination = outWithGenreAndFormat + File.separator;
    final var destination = new File(finalDestination + source.getName());
    if (!genreDir.exists()) {
      createDirectory(genreDir);
    }
    if (!destination.exists()) {
      createDirectory(destination);
    }
    moveDirectory(sourceDirectory, source, destination);
  }

  private void verifyCheckSum(File source) {
    final List<Path> paths =
        Try.withResources(() -> Files.list(source.toPath()))
            .of(pathStream -> pathStream.collect(Collectors.toList()))
            .get();

    final Optional<Path> sfv =
        paths.parallelStream().filter(path -> path.endsWith("sfv")).findFirst();

    final List<String> strings =
        Try.of(() -> Files.readAllLines(sfv.orElseThrow(FileNotFoundException::new)))
            .onFailure(t -> error("Error reading lines: {}, {}", sfv.toString(), t.getMessage(), t))
            .get();
    final Map<String, String> collect =
        strings.parallelStream()
            .map(s -> s.split(" "))
            .collect(Collectors.toMap(res -> res[0], res -> res.length > 1 ? res[1] : ""));

    for (Path path : paths) {
      // TODO calc crc, get by key from map, compare calculated crc and crc from sfv file
    }
  }

  private Map<?, ?> hasCheckSum(File source) {
    final List<Path> sfv =
        Try.withResources(() -> Files.list(source.toPath()))
            .of(
                pathStream ->
                    pathStream
                        .filter(path -> path.getFileName().endsWith("sfv"))
                        .collect(Collectors.toList()))
            .onFailure(throwable -> info("No Checksum file for {}", source.toString()))
            .getOrElse(Collections.emptyList());
    if (sfv.isEmpty()) {
      return Collections.emptyMap();
    } else {
      return Map.of(CHECKSUM, sfv.get(0));
    }
  }

  private Set<String> listAlbums() {
    return Try.withResources(
            () ->
                Files.walk(
                    Paths.get(propertiesService.getProperties().getSourceFolder()),
                    Integer.MAX_VALUE))
        .of(
            stream -> {
              final Set<String> dirs = new HashSet<>();
              stream
                  .filter(IS_MUSIC_FILE)
                  .collect(Collectors.toSet())
                  .forEach(o -> dirs.add(o.getParent().toString()));
              if (propertiesService.getProperties().isSkipLiveReleases()) {
                return dirs.stream()
                    .filter(
                        folderName ->
                            isNotLiveRelease(folderName, propertiesService.getProperties()))
                    .collect(Collectors.toSet());
              } else {
                return dirs;
              }
            })
        .onFailure(
            e ->
                error(
                    "Error while walking directory: {}, {}",
                    propertiesService.getProperties().getSourceFolder(),
                    e.getMessage(),
                    e))
        .getOrElse(new HashSet<>());
  }

  @Override
  public void processDirectories() {
    var folderList = listAlbums();
    if (folderList.isEmpty()) {
      info("No data to work with. Exiting.");
      System.exit(0);
    }

    info("folders list created. size: {}", folderList.size());
    final var result = tryWithProgressBar(folderList, propertiesService.getProperties());
    if (result) {
      info("Finished. Cleaning empty directories ...");
      cleanUpService.cleanUpParentDirectory();
      System.exit(0);
    }
  }

  /*need*/
  @Override
  public ListingWithFormat createFileListForEachDir(final String folderName) {
    /*creating filter for musical files*/
    final List<String> resultMp3 = new ArrayList<>();
    final List<String> resultFlac = new ArrayList<>();
    var result = new ListingWithFormat();
    listFiles(folderName, resultMp3, MP3);
    listFiles(folderName, resultFlac, FLAC);
    if (!resultMp3.isEmpty()) {
      result = ListingWithFormat.builder().format(MP_3_FORMAT).fileList(resultMp3).build();
    } else if (!resultFlac.isEmpty()) {
      result = ListingWithFormat.builder().format(FLAC_FORMAT).fileList(resultFlac).build();
    }
    return result;
  }
}
