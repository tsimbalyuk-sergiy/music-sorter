package dev.tsvinc.music.sort.service;

import static org.pmw.tinylog.Logger.error;

import io.vavr.control.Try;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class CleanUpServiceImpl implements CleanUpService {
  @Inject PropertiesService propertiesService;

  private static boolean isEmptyDirectory(final Path path) {
    return Try.withResources(() -> Files.list(path))
        .of(stream -> 0 >= stream.count())
        .onFailure(e -> error("Error listing directory: {}, {}", path, e.getMessage()))
        .getOrElse(false);
  }

  public void cleanUpParentDirectory() {
    final var listOfEmptyDirectories =
        new AtomicReference<>(
            CleanUpServiceImpl.getListOfEmptyDirectories(
                Paths.get(this.propertiesService.getProperties().getSourceFolder())));
    while (!listOfEmptyDirectories.get().isEmpty()) {
      Try.of(
              () -> {
                listOfEmptyDirectories
                    .get()
                    .forEach(
                        dir -> {
                          try {
                            Files.delete(dir);
                          } catch (final IOException ioe) {
                            error("Error deleting folder: {} {}\n{}", dir, ioe.getMessage(), ioe);
                          }
                        });
                listOfEmptyDirectories.set(
                    CleanUpServiceImpl.getListOfEmptyDirectories(
                        Paths.get(this.propertiesService.getProperties().getSourceFolder())));
                return null;
              })
          .onFailure(
              e ->
                  error(
                      "Error while walking directory: {}, {}",
                      this.propertiesService.getProperties().getSourceFolder(),
                      e.getMessage(),
                      e));
    }
  }

  private static List<Path> getListOfEmptyDirectories(final Path directory) {
    return Try.withResources(() -> Files.walk(directory, Integer.MAX_VALUE))
        .of(
            stream ->
                stream
                    .filter(Files::isDirectory)
                    .filter(CleanUpServiceImpl::isEmptyDirectory)
                    .collect(Collectors.toList()))
        .onFailure(e -> error("Error walking directory: {}, {}", directory, e.getMessage(), e))
        .getOrElse(Collections.emptyList());
  }
}
