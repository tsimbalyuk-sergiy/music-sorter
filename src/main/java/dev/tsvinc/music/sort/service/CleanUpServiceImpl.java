package dev.tsvinc.music.sort.service;

import static org.pmw.tinylog.Logger.error;

import io.vavr.control.Try;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class CleanUpServiceImpl implements CleanUpService {
  @Inject PropertiesService propertiesService;

  public void cleanUpParentDirectory() {
    while (!directoryIsEmpty(Paths.get(propertiesService.getProperties().getSourceFolder()))) {
      Try.withResources(
              () ->
                  Files.walk(
                      Paths.get(propertiesService.getProperties().getSourceFolder()),
                      Integer.MAX_VALUE))
          .of(
              stream -> {
                stream
                    .filter(path -> path.toFile().isDirectory())
                    .filter(CleanUpServiceImpl::directoryIsEmpty)
                    .collect(Collectors.toList())
                    .forEach(
                        dir -> {
                          try {
                            Files.delete(dir);
                          } catch (final IOException ioe) {
                            error("Error deleting folder: {} {}\n{}", dir, ioe.getMessage(), ioe);
                          }
                        });
                return null;
              })
          .onFailure(
              e ->
                  error(
                      "Error while walking directory: {}, {}",
                      propertiesService.getProperties().getSourceFolder(),
                      e.getMessage(),
                      e));
    }
  }

  /**
   * Checks given directory for being empty
   *
   * @param directory {@link Path} path that represents actual directory
   * @return {@link Boolean}
   */
  private static boolean directoryIsEmpty(final Path directory) {
    return Try.withResources(() -> Files.newDirectoryStream(directory))
        .of(paths -> !paths.iterator().hasNext())
        .onFailure(
            e ->
                error(
                    "Error checking if directory is empty: {}, {}, {}",
                    directory,
                    e.getMessage(),
                    e))
        .getOrElse(false);
  }
}
