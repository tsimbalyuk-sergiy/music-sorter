package dev.tsvinc.music.sort.service;

import io.vavr.control.Try;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.tinylog.Logger.error;

public class CleanUpServiceImpl implements CleanUpService {
    @Inject
    PropertiesService propertiesService;

    private static boolean isEmptyDirectory(final Path path) {
        return Try.withResources(() -> Files.list(path))
                .of(stream -> stream.parallel().findAny().isEmpty())
                .onFailure(e -> error("Error listing directory: {}, {}", path, e.getMessage()))
                .getOrElse(false);
    }

    private static void deleteEachEmptyDirectory(final AtomicReference<List<Path>> listOfEmptyDirectories) {
        listOfEmptyDirectories.get().stream().parallel().forEach(CleanUpServiceImpl::safelyDeleteDirectory);
    }

    private void logErrorWhileWalkingDirectory(final Throwable e) {
        error(
                "Error while walking directory: {}, {}",
                this.propertiesService.getProperties().sourceFolder(),
                e.getMessage(),
                e);
    }

    private static List<Path> getListOfEmptyDirectories(final Path directory) {
        return Try.withResources(() -> Files.walk(directory, Integer.MAX_VALUE))
                .of(stream -> stream.parallel()
                        .filter(Files::isDirectory)
                        .filter(path -> !path.equals(directory))
                        .filter(CleanUpServiceImpl::isEmptyDirectory)
                        .toList())
                .onFailure(e -> error("Error walking directory: {}, {}", directory, e.getMessage(), e))
                .getOrElse(Collections.emptyList());
    }

    private static void safelyDeleteDirectory(final Path dir) {
        try {
            Files.delete(dir);
        } catch (final IOException ioe) {
            error("Error deleting folder: {} {}\n{}", dir, ioe.getMessage(), ioe);
        }
    }

    public void cleanUpParentDirectory() {
        Path directory = Paths.get(this.propertiesService.getProperties().sourceFolder());
        final var listOfEmptyDirectories = new AtomicReference<>(getListOfEmptyDirectories(directory));
        while (!listOfEmptyDirectories.get().isEmpty()) {
            Try.of(() -> {
                        deleteEachEmptyDirectory(listOfEmptyDirectories);
                        listOfEmptyDirectories.set(getListOfEmptyDirectories(directory));
                        return null;
                    })
                    .onFailure(this::logErrorWhileWalkingDirectory);
        }
    }
}
